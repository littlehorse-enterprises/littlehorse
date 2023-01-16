package io.littlehorse.server.streamsimpl;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.CentralStoreQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryPb.CentralStoreSubQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.HostInfoPb;
import io.littlehorse.common.proto.InternalGetAdvertisedHostsPb;
import io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsStub;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PaginatedTagQueryPb;
import io.littlehorse.common.proto.PaginatedTagQueryReplyPb;
import io.littlehorse.common.proto.PartitionBookmarkPb;
import io.littlehorse.common.proto.RegisterTaskWorkerPb;
import io.littlehorse.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.storeinternals.LHROStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsimpl.util.AsyncWaiters;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsMetadata;
import org.apache.kafka.streams.TaskMetadata;
import org.apache.kafka.streams.ThreadMetadata;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class BackendInternalComms implements Closeable {

    private LHConfig config;
    private Server internalGrpcServer;
    private KafkaStreams coreStreams;
    private HostInfo thisHost;
    private LHProducer producer;

    private Map<String, ManagedChannel> channels;
    // private ConcurrentHashMap<String, StreamObserver<WaitForCommandReplyPb>> asyncWaiters;
    private AsyncWaiters asyncWaiters;
    private ConcurrentHashMap<HostInfo, InternalGetAdvertisedHostsReplyPb> otherHosts;

    public BackendInternalComms(LHConfig config, KafkaStreams coreStreams) {
        this.config = config;
        this.coreStreams = coreStreams;
        this.channels = new HashMap<>();
        otherHosts = new ConcurrentHashMap<>();

        this.internalGrpcServer =
            ServerBuilder
                .forPort(this.config.getInternalBindPort())
                .addService(new InterBrokerCommServer())
                .build();

        thisHost =
            new HostInfo(
                config.getInternalAdvertisedHost(),
                config.getInternalAdvertisedPort()
            );
        this.producer = new LHProducer(config, false);
        this.asyncWaiters = new AsyncWaiters();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20 * 1000);
                    this.asyncWaiters.cleanupOldWaiters();
                } catch (InterruptedException exn) {
                    throw new RuntimeException(exn);
                }
            }
        })
            .start();
    }

    public void start() throws IOException {
        internalGrpcServer.start();
    }

    public void close() {
        LHUtil.log("Closing backend internal comms");
        for (ManagedChannel channel : channels.values()) {
            channel.shutdown();
        }
        internalGrpcServer.shutdownNow();
        try {
            internalGrpcServer.awaitTermination();
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }
    }

    /**
     * Performs a distributed point query by hashing the partition key and querying
     * the backing RocksDB store on the resulting host. This method is asynchronous;
     * so you provide a StreamObserver (which is a handy interface by grpc) which is
     * callback'ed once the data is available.
     *
     * Internally, this method uses the grpc async client.
     *
     * @param fullStoreKey is the FULL store key to query.
     * @param partitionKey is the partition key.
     * @param observer is the callback-able object.
     */
    public void getBytesAsync(
        String fullStoreKey,
        String partitionKey,
        StreamObserver<CentralStoreQueryReplyPb> observer
    ) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            localGetBytesAsync(fullStoreKey, observer);
        } else {
            queryRemoteAsync(
                meta,
                CentralStoreSubQueryPb.newBuilder().setKey(fullStoreKey).build(),
                observer
            );
        }
    }

    private void localGetBytesAsync(
        String fullStoreKey,
        StreamObserver<CentralStoreQueryReplyPb> observer
    ) {
        ReadOnlyKeyValueStore<String, Bytes> store = getRawStore(null, false);
        Bytes result = store.get(fullStoreKey);

        CentralStoreQueryReplyPb.Builder out = CentralStoreQueryReplyPb
            .newBuilder()
            .setCode(StoreQueryStatusPb.RSQ_OK);
        if (result != null) out.setResult(ByteString.copyFrom(result.get()));

        observer.onNext(out.build());
    }

    public TaskScheduleRequest getTsr(String tsrId) {
        return new LHROStoreWrapper(getRawStore(null, false), config)
            .get(tsrId, TaskScheduleRequest.class);
    }

    public void getLastFromPrefixAsync(
        String prefix,
        String partitionKey,
        StreamObserver<CentralStoreQueryReplyPb> observer
    ) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            LHROStoreWrapper wrapper = new LHROStoreWrapper(
                getRawStore(null, false),
                config
            );
            Bytes result = wrapper.getLastBytesFromFullPrefix(prefix);
            CentralStoreQueryReplyPb.Builder out = CentralStoreQueryReplyPb.newBuilder();
            out.setCode(StoreQueryStatusPb.RSQ_OK);
            if (result != null) {
                out.setResult(ByteString.copyFrom(result.get()));
            }
            observer.onNext(out.build());
        } else {
            queryRemoteAsync(
                meta,
                CentralStoreSubQueryPb.newBuilder().setLastFromPrefix(prefix).build(),
                observer
            );
        }
    }

    // EMPLOYEE_TODO: determine if we can use generics here to provide some guards
    // against passing in a Command that's incompatible with the POSTStreamObserver.
    public void waitForCommand(
        Command command,
        StreamObserver<WaitForCommandReplyPb> observer
    ) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            command.getPartitionKey(),
            Serdes.String().serializer()
        );
        if (command.commandId == null) {
            command.commandId = LHUtil.generateGuid();
        }

        /*
         * Decided that everything is just simpler if we insist upon the server which
         * is responsible for processing the command also being the server that sends
         * the command to Kafka. That's because we need to make sure that the
         */
        if (meta.activeHost().equals(thisHost)) {
            localWaitForCommand(command.commandId, observer);
        } else {
            getInternalAsyncClient(meta.activeHost())
                .waitForCommand(
                    WaitForCommandPb
                        .newBuilder()
                        .setCommandId(command.commandId)
                        .build(),
                    observer
                );
        }
    }

    // EMPLOYEE_TODO: implement a mechanism whereby task worker groups split the
    // LH Server instances among them.
    public RegisterTaskWorkerReplyPb registerTaskWorker(RegisterTaskWorkerPb req) {
        Collection<StreamsMetadata> allMeta = coreStreams.metadataForAllStreamsClients();

        // TODO: in more optimized future versions, we will communicate about which
        // clients are assigned to which hosts in order to be smarter and less dumb
        Set<HostInfo> hosts = new HashSet<>();

        for (StreamsMetadata meta : allMeta) {
            hosts.add(meta.hostInfo());
        }

        RegisterTaskWorkerReplyPb.Builder out = RegisterTaskWorkerReplyPb.newBuilder();
        out.setCode(LHResponseCodePb.OK);

        for (HostInfo host : hosts) {
            InternalGetAdvertisedHostsReplyPb advertisedHostsForHost = getPublicListenersForHost(
                host
            );
            if (advertisedHostsForHost == null) {
                LHUtil.log("Warn: host", host.host(), host.port(), "unreachable");
                continue;
            }

            HostInfoPb desiredHost = advertisedHostsForHost.getHostsOrDefault(
                req.getListenerName(),
                null
            );
            if (desiredHost == null) {
                out.setCode(LHResponseCodePb.BAD_REQUEST_ERROR);
                out.setMessage(
                    "Unknown listener name. Check LHORSE_ADVERTISED_LISTENERS on " +
                    "LH Server and check the LISTENER_NAME config on task worker."
                );
                out.clearAllHosts();
                return out.build();
            }

            out.addAllHosts(desiredHost);
        }

        return out.build();
    }

    private InternalGetAdvertisedHostsReplyPb getPublicListenersForHost(
        HostInfo streamsHost
    ) {
        if (otherHosts.get(streamsHost) != null) {
            return otherHosts.get(streamsHost);
        }

        try {
            InternalGetAdvertisedHostsReplyPb info = getInternalClient(streamsHost)
                .getAdvertisedHosts(
                    InternalGetAdvertisedHostsPb.newBuilder().build()
                );

            otherHosts.put(streamsHost, info);
            return info;
        } catch (Exception exn) {
            return null;
        }
    }

    public LHProducer getProducer() {
        return producer;
    }

    public void onResponseReceived(String commandId, WaitForCommandReplyPb response) {
        asyncWaiters.put(commandId, response);
    }

    private void localWaitForCommand(
        String commandId,
        StreamObserver<WaitForCommandReplyPb> observer
    ) {
        asyncWaiters.put(commandId, observer);
        // Once the command has been recorded, we've got nothing to do: the
        // CommandProcessor will notify the StreamObserver once the command is
        // processed.
    }

    public void recordCommand(Command command) {
        // Next we record the command.
    }

    private ReadOnlyKeyValueStore<String, Bytes> getRawStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
            ServerTopology.CORE_STORE,
            QueryableStoreTypes.keyValueStore()
        );

        if (enableStaleStores) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        return coreStreams.store(params);
    }

    private LHROStoreWrapper getLocalStore(
        Integer specificPartition,
        boolean enableStaleStores
    ) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(
            specificPartition,
            enableStaleStores
        );
        return new LHROStoreWrapper(rawStore, config);
    }

    /**
     * Performs an RPC call to remotely query the host specified by the provided
     * KeyQueryMetadata, and calls the callback provided in the StreamObserver
     * upon completion of the call.
     *
     * This method uses the gRPC async client.
     *
     * The original version was synchronous and included rpc's to the standby hosts
     * in the case that the active host was unavailable.
     *
     * EMPLOYEE_TODO: re-enable that functionality in the async environment.
     * @param meta is the metadata for the partion-key that we're searching for.
     * @param subQuery is the actual subquery we want to ask.
     */
    private void queryRemoteAsync(
        KeyQueryMetadata meta,
        CentralStoreSubQueryPb subQuery,
        StreamObserver<CentralStoreQueryReplyPb> observer
    ) {
        // todo
        LHInternalsStub client = getInternalAsyncClient(meta.activeHost());
        client.centralStoreQuery(
            CentralStoreQueryPb
                .newBuilder()
                .setEnableStaleStores(false)
                .setSpecificPartition(meta.partition())
                .setQuery(subQuery)
                .build(),
            observer
        );
    }

    private LHInternalsBlockingStub getInternalClient(HostInfo host) {
        return LHInternalsGrpc.newBlockingStub(getChannel(host));
    }

    private LHInternalsStub getInternalAsyncClient(HostInfo host) {
        return LHInternalsGrpc.newStub(getChannel(host));
    }

    private ManagedChannel getChannel(HostInfo host) {
        String key = host.host() + ":" + host.port();
        ManagedChannel channel = channels.get(key);
        if (channel == null) {
            channel =
                ManagedChannelBuilder
                    .forAddress(host.host(), host.port())
                    .usePlaintext()
                    .build();
            channels.put(key, channel);
        }
        return channel;
    }

    /*
     * Implements the internal_server.proto service, which is used
     * for communication between the LH servers to do distributed lookups etc.
     */
    private class InterBrokerCommServer extends LHInternalsImplBase {

        /*
         * Need to investigate:
         * - Behavior when not owner of partition
         * - What exception is thrown when the store is unavailable (eg. REBALANCING)?
         *
         * TODO for first hire: Figure out how to set the "approximateLag" field on
         * the response.
         */
        @Override
        public void centralStoreQuery(
            CentralStoreQueryPb req,
            StreamObserver<CentralStoreQueryReplyPb> ctx
        ) {
            Integer specificPartition = null;
            if (req.hasSpecificPartition()) {
                specificPartition = req.getSpecificPartition();
            }

            CentralStoreQueryReplyPb.Builder out = CentralStoreQueryReplyPb.newBuilder();

            ReadOnlyKeyValueStore<String, Bytes> rawStore;
            try {
                rawStore = getRawStore(specificPartition, req.getEnableStaleStores());
            } catch (Exception exn) {
                exn.printStackTrace();
                out.setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE);
                ctx.onNext(out.build());
                ctx.onCompleted();
                return;
            }
            Bytes result = null;

            switch (req.getQuery().getQueryCase()) {
                case KEY:
                    result = rawStore.get(req.getQuery().getKey());
                    break;
                case LAST_FROM_PREFIX:
                    result =
                        new LHROStoreWrapper(rawStore, config)
                            .getLastBytesFromFullPrefix(
                                req.getQuery().getLastFromPrefix()
                            );
                    break;
                case QUERY_NOT_SET:
                default:
                    throw new RuntimeException("Not possible");
            }

            out.setCode(StoreQueryStatusPb.RSQ_OK);
            if (result != null) {
                out.setResult(ByteString.copyFrom(result.get()));
            }

            ctx.onNext(out.build());
            ctx.onCompleted();
        }

        @Override
        public void paginatedTagQuery(
            PaginatedTagQueryPb req,
            StreamObserver<PaginatedTagQueryReplyPb> ctx
        ) {
            ctx.onNext(localPaginatedTagQuery(req));
            ctx.onCompleted();
        }

        @Override
        public void waitForCommand(
            WaitForCommandPb req,
            StreamObserver<WaitForCommandReplyPb> ctx
        ) {
            localWaitForCommand(req.getCommandId(), ctx);
        }

        @Override
        public void getAdvertisedHosts(
            InternalGetAdvertisedHostsPb req,
            StreamObserver<InternalGetAdvertisedHostsReplyPb> ctx
        ) {
            Map<String, HostInfoPb> hosts = config.getPublicAdvertisedHostMap();
            InternalGetAdvertisedHostsReplyPb.Builder out = InternalGetAdvertisedHostsReplyPb.newBuilder();

            out.putAllHosts(hosts);

            ctx.onNext(out.build());
            ctx.onCompleted();
        }
    }

    public PaginatedTagQueryReplyPb doPaginatedTagQuery(PaginatedTagQueryPb req)
        throws LHConnectionError {
        // First, see what results we have locally. Then if we need more results
        // to hit the limit, we query another host.
        // How do we know which host to query? Well, we find a partition which
        // hasn't been completed yet (by consulting the Bookmark), and then
        // query the owner of that partition.

        PaginatedTagQueryReplyPb out = localPaginatedTagQuery(req);
        if (out.getObjectIdsCount() >= req.getLimit()) {
            // Then we've gotten all the data the client asked for.
            return out;
        }
        if (!out.hasUpdatedBookmark()) {
            // Then we've gotten all the data there is.
            return out;
        }

        // OK, now we need to figure out a host to query.

        // We *know* that if a partition is in the bookmark, then that partition
        // is still in progress. We can infer that the partition probably doesn't
        // live on this host, because otherwise the query would have either pulled
        // to the end of that partition or returned because it reached the limit.
        // Note however, it's POSSIBLE that the partition lives on this host if
        // there was a rebalance between the first request and the second request.
        // That's quite unlikely.

        // Basically, what we need to do is find the set of all partitions that
        // AREN'T in the BookmarkPb::getCompletedPartitionsList();
        while (out.hasUpdatedBookmark() && out.getObjectIdsCount() < req.getLimit()) {
            HostInfo otherHost = getHostForPartition(
                getRandomUnfinishedPartition(out.getUpdatedBookmark())
            );
            if (otherHost.equals(thisHost)) {
                throw new RuntimeException("wtf, host the same");
            }
            LHInternalsBlockingStub stub = getInternalClient(otherHost);
            PaginatedTagQueryPb newReq = PaginatedTagQueryPb
                .newBuilder()
                .setBookmark(out.getUpdatedBookmark())
                .setLimit(req.getLimit() - out.getObjectIdsCount())
                .setObjectType(req.getObjectType())
                .addAllAttributes(req.getAttributesList())
                .build();
            PaginatedTagQueryReplyPb reply;
            try {
                reply = stub.paginatedTagQuery(newReq);
            } catch (Exception exn) {
                throw new LHConnectionError(exn, "Failed connecting to backend.");
            }
            if (reply.getCode() != StoreQueryStatusPb.RSQ_OK) {
                throw new LHConnectionError(null, "Failed connecting to backend.");
            }
            PaginatedTagQueryReplyPb.Builder newOutBuilder = PaginatedTagQueryReplyPb
                .newBuilder()
                .addAllObjectIds(out.getObjectIdsList())
                .addAllObjectIds(reply.getObjectIdsList());
            if (reply.hasUpdatedBookmark()) {
                newOutBuilder.setUpdatedBookmark(reply.getUpdatedBookmark());
            } else {
                newOutBuilder.clearUpdatedBookmark();
            }
            out = newOutBuilder.build();
        }

        return out;
    }

    private HostInfo getHostForPartition(int partition) {
        if (partition >= config.getClusterPartitions()) {
            throw new RuntimeException("Colt you need to sleep more you moron");
        }

        // This is O(N) where N is # of partitions...not too great yikerz
        Collection<StreamsMetadata> all = coreStreams.metadataForAllStreamsClients();
        for (StreamsMetadata meta : all) {
            for (TopicPartition tp : meta.topicPartitions()) {
                if (tp.partition() != partition) continue;
                if (isCommandProcessor(tp, config)) {
                    return meta.hostInfo();
                }
            }
        }
        return null;
    }

    private Set<Integer> getUnfininshedPartitions(BookmarkPb bm) {
        Set<Integer> out = new HashSet<>();
        for (int i = 0; i < config.getClusterPartitions(); i++) {
            out.add(i);
        }
        out.removeAll(bm.getCompletedPartitionsList());

        return out;
    }

    private int getRandomUnfinishedPartition(BookmarkPb bm) {
        Set<Integer> unfinished = getUnfininshedPartitions(bm);
        for (int i : unfinished) {
            return i;
        }
        throw new RuntimeException("Not possible");
    }

    private PaginatedTagQueryReplyPb localPaginatedTagQuery(PaginatedTagQueryPb req) {
        int curLimit = req.getLimit();

        BookmarkPb reqBookmark = req.hasBookmark()
            ? req.getBookmark()
            : BookmarkPb.newBuilder().build();

        BookmarkPb.Builder outBookmark = reqBookmark.toBuilder();
        PaginatedTagQueryReplyPb.Builder out = PaginatedTagQueryReplyPb.newBuilder();
        List<Attribute> attrList = new ArrayList<>();
        for (AttributePb atpb : req.getAttributesList()) {
            attrList.add(Attribute.fromProto(atpb));
        }

        // iterate through all active and standby local partitions
        for (int partition : getLocalCommandProcessorPartitions()) {
            LHROStoreWrapper partStore = getLocalStore(partition, false);
            if (reqBookmark.getCompletedPartitionsList().contains(partition)) {
                // This partition has already been accounted for
                continue;
            }
            PartitionBookmarkPb partBookmark = null;
            if (reqBookmark != null) {
                partBookmark =
                    reqBookmark.getInProgressPartitionsOrDefault(partition, null);
            }

            // Add all matching objects from that partition
            Pair<Set<String>, PartitionBookmarkPb> result = onePartitionPaginatedTagScan(
                attrList,
                partBookmark,
                curLimit,
                req.getObjectType(),
                partition,
                partStore
            );

            curLimit -= result.getLeft().size();
            out.addAllObjectIds(result.getLeft());
            PartitionBookmarkPb thisPartionBookmark = result.getRight();
            if (thisPartionBookmark == null) {
                // then the partition is done
                outBookmark.addCompletedPartitions(partition);
                outBookmark.removeInProgressPartitions(partition);
            } else {
                outBookmark.putInProgressPartitions(partition, thisPartionBookmark);
            }

            if (curLimit == 0) {
                break;
            }
            if (curLimit < 0) {
                throw new RuntimeException("WTF?");
            }
        }

        if (
            outBookmark.getCompletedPartitionsCount() < config.getClusterPartitions()
        ) {
            out.setUpdatedBookmark(outBookmark);
        } else {
            // Then every partition has been scanned, so the paginated query is
            // complete. That means we don't return a bookmark.
            out.clearUpdatedBookmark();
        }
        return out.build();
    }

    private Pair<Set<String>, PartitionBookmarkPb> onePartitionPaginatedTagScan(
        List<Attribute> attributes,
        PartitionBookmarkPb bookmark,
        int limit,
        GETableClassEnumPb objectType,
        int partition,
        LHROStoreWrapper store
    ) {
        PartitionBookmarkPb bmOut = null;
        Set<String> idsOut = new HashSet<>();

        String endKey =
            Tag.getAttributeString(objectType, attributes) + "~~~~~~~~~~~";
        String startKey;
        if (bookmark == null) {
            startKey = Tag.getAttributeString(objectType, attributes) + "/";
        } else {
            startKey = bookmark.getLastKey();
        }

        try (
            LHKeyValueIterator<Tag> iter = store.range(startKey, endKey, Tag.class)
        ) {
            boolean brokenBecauseOutOfData = true;
            while (iter.hasNext()) {
                LHIterKeyValue<Tag> next = iter.next();
                Tag tag = next.getValue();
                if (--limit < 0) {
                    bmOut =
                        PartitionBookmarkPb
                            .newBuilder()
                            .setParttion(partition)
                            .setLastKey(tag.getObjectId())
                            .build();

                    // broke loop because we filled up the limit
                    brokenBecauseOutOfData = false;
                    break;
                }

                idsOut.add(tag.describedObjectId);
            }

            if (brokenBecauseOutOfData) {
                bmOut = null;
            }
        }
        return Pair.of(idsOut, bmOut);
    }

    private Set<Integer> getLocalCommandProcessorPartitions() {
        Set<Integer> out = new HashSet<>();

        for (ThreadMetadata thread : coreStreams.metadataForLocalThreads()) {
            for (TaskMetadata activeTask : thread.activeTasks()) {
                // We only want to query
                if (isCommandProcessor(activeTask, config)) {
                    out.add(activeTask.taskId().partition());
                }
            }
            for (TaskMetadata activeTask : thread.standbyTasks()) {
                if (isCommandProcessor(activeTask, config)) {
                    out.add(activeTask.taskId().partition());
                }
            }
        }
        return out;
    }

    private static boolean isCommandProcessor(TaskMetadata task, LHConfig config) {
        for (TopicPartition tPart : task.topicPartitions()) {
            if (isCommandProcessor(tPart, config)) return true;
        }
        return false;
    }

    private static boolean isCommandProcessor(TopicPartition tPart, LHConfig config) {
        return tPart.topic().equals(config.getCoreCmdTopicName());
    }
}
