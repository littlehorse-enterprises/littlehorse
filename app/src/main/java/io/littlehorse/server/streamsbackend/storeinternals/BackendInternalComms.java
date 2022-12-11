package io.littlehorse.server.streamsbackend.storeinternals;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommandresponse.TaskClaimReply;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.CentralStoreQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryPb.CentralStoreSubQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.CommandPb.CommandCase;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.InternalPollTaskPb;
import io.littlehorse.common.proto.InternalPollTaskReplyPb;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.PaginatedTagQueryPb;
import io.littlehorse.common.proto.PaginatedTagQueryReplyPb;
import io.littlehorse.common.proto.PartitionBookmarkPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResultPb;
import io.littlehorse.common.proto.WaitForCommandResultReplyPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.KafkaStreamsBackend;
import io.littlehorse.server.streamsbackend.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.index.Attribute;
import io.littlehorse.server.streamsbackend.storeinternals.index.DiscreteTagLocalCounter;
import io.littlehorse.server.streamsbackend.storeinternals.index.Tag;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHKeyValueIterator;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private InflightList inflightList;
    private KafkaStreamsBackend upperLayer;

    private Map<String, ManagedChannel> channels;

    public BackendInternalComms(
        LHConfig config,
        KafkaStreams coreStreams,
        KafkaStreamsBackend upperLayer
    ) {
        this.config = config;
        this.coreStreams = coreStreams;
        this.channels = new HashMap<>();
        this.inflightList = new InflightList();
        this.upperLayer = upperLayer;

        this.internalGrpcServer =
            ServerBuilder
                .forPort(this.config.getInternalBindPort())
                .addService(new InterBrokerCommServer())
                .build();

        thisHost =
            new HostInfo(
                config.getAdvertisedHost(),
                config.getInternalAdvertisedPort()
            );
    }

    public void start() throws IOException {
        internalGrpcServer.start();
    }

    public void close() {
        internalGrpcServer.shutdown();
        for (ManagedChannel channel : channels.values()) {
            channel.shutdown();
        }
    }

    public Bytes getBytes(String fullStoreKey, String partitionKey)
        throws LHConnectionError {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            return getRawStore(null, false).get(fullStoreKey);
        } else {
            return queryRemote(
                meta,
                CentralStoreSubQueryPb.newBuilder().setKey(fullStoreKey).build()
            );
        }
    }

    public TaskScheduleRequest pollTask(String taskDefName) throws LHConnectionError {
        // First attempt: check our local active partitions
        TaskScheduleRequest local = findTaskLocally(taskDefName);
        if (local != null) {
            return local;
        }

        // Now we gotta see if the other hosts have any pending tasks. To do that,
        // we check the global store for the counters on the tags (since the
        // TagStorageType for the relevant tag is LOCAL_COUNTED).
        Exception caught = null;
        for (HostInfo other : findHostsWithPendingTask(taskDefName)) {
            LHUtil.log("polling for task remotely: ", other.host(), other.port());

            LHInternalsBlockingStub client = getInternalClient(other);
            try {
                InternalPollTaskReplyPb resp = client.internalPollTask(
                    InternalPollTaskPb
                        .newBuilder()
                        .setTaskQueueName(taskDefName)
                        .build()
                );
                if (resp.hasResult()) {
                    return TaskScheduleRequest.fromProto(resp.getResultOrBuilder());
                }
            } catch (Exception exn) {
                caught = exn;
            }
        }

        if (caught != null) {
            throw new LHConnectionError(
                caught,
                "Failed contacting internal LH brokers."
            );
        }
        return null;
    }

    // OPTIMIZATION IDEA: in the future, we should cache this call and have it
    // update every 500ms or so for each queue so that we don't have thousands of
    // range scans going on all the time.
    // Better yet, we could also have the global store processor send updates to
    // the cache object (that is probably the reason why providing a processor
    // is allowed in the first place).
    private Set<HostInfo> findHostsWithPendingTask(String taskDefName) {
        LHROStoreWrapper store = getGlobalStore();
        String prefix = Tag.getAttributeString(
            GETableClassEnumPb.TASK_SCHEDULE_REQUEST,
            Arrays.asList(new Attribute("taskDefName", taskDefName))
        );

        Map<Integer, Long> partitionsToCounts = new HashMap<>();
        try (
            LHKeyValueIterator<DiscreteTagLocalCounter> iter = store.prefixScan(
                prefix,
                DiscreteTagLocalCounter.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<DiscreteTagLocalCounter> next = iter.next();
                DiscreteTagLocalCounter counter = next.getValue();
                partitionsToCounts.put(counter.partition, counter.localCount);
            }
        }
        // TODO: eventually we're gonna do something more intelligent regarding
        // choosing hosts that have the most pending tasks on them.
        Set<HostInfo> out = new HashSet<>();
        for (Map.Entry<Integer, Long> entry : partitionsToCounts.entrySet()) {
            out.add(getHostForPartition(entry.getKey()));
        }
        out.remove(thisHost);
        return out;
    }

    private TaskScheduleRequest findTaskLocally(String taskDefName)
        throws LHConnectionError {
        LHROStoreWrapper allActivePartitions = getLocalStore(null, false);
        try (
            LHKeyValueIterator<Tag> tagIter = allActivePartitions.prefixScan(
                Tag.getAttributeString(
                    GETableClassEnumPb.TASK_SCHEDULE_REQUEST,
                    Arrays.asList(new Attribute("taskDefName", taskDefName))
                ),
                Tag.class
            )
        ) {
            while (tagIter.hasNext()) {
                LHIterKeyValue<Tag> next = tagIter.next();
                Tag tag = next.getValue();
                String taskScheduleReqId = tag.describedObjectId;
                if (inflightList.markInFlight(taskDefName, taskScheduleReqId)) {
                    TaskScheduleRequest tsr = allActivePartitions.get(
                        taskScheduleReqId,
                        TaskScheduleRequest.class
                    );

                    // We're like 99% sure that we have it now; but still, we have
                    // to wait for processing to ensure correctness in the case of
                    // unclean shutdown + recovery. Otherwise, we would just
                    // fire off the event and return the tsr.
                    return claimLocalTask(tsr).result;
                }
            }
        }
        return null;
    }

    /*
     * Returns true if the task is successfully claimed.
     */
    private TaskClaimReply claimLocalTask(TaskScheduleRequest req)
        throws LHConnectionError {
        // It's been marked on the inflight list, so we don't need to worry about
        // that.
        TaskClaimEvent tse = new TaskClaimEvent();
        tse.wfRunId = req.wfRunId;
        tse.threadRunNumber = req.threadRunNumber;
        tse.taskRunPosition = req.taskRunPosition;
        tse.taskRunNumber = req.taskRunNumber;
        tse.time = new Date();

        Command taskClaimCommand = new Command();
        taskClaimCommand.type = CommandCase.TASK_CLAIM_EVENT;
        taskClaimCommand.taskClaimEvent = tse;

        return upperLayer.process(tse, TaskClaimReply.class);
    }

    public Bytes getLastFromPrefix(String prefix, String partitionKey)
        throws LHConnectionError {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            return new LHROStoreWrapper(getRawStore(null, false), config)
                .getLastBytesFromFullPrefix(prefix);
        } else {
            return queryRemote(
                meta,
                CentralStoreSubQueryPb.newBuilder().setLastFromPrefix(prefix).build()
            );
        }
    }

    public Bytes waitForProcessing(Command command) throws LHConnectionError {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            ServerTopology.CORE_STORE,
            command.getPartitionKey(),
            Serdes.String().serializer()
        );

        LHInternalsBlockingStub client = getInternalClient(meta.activeHost());

        WaitForCommandResultReplyPb resp;
        try {
            resp =
                client.waitForCommandResult(
                    WaitForCommandResultPb
                        .newBuilder()
                        .setCommandId(command.commandId)
                        .setSpecificPartition(meta.partition())
                        .build()
                );
        } catch (Exception exn) {
            throw new LHConnectionError(
                exn,
                "Could not connect to required LH broker."
            );
        }

        switch (resp.getCode()) {
            case RSQ_OK:
                if (resp.hasResult()) {
                    // lol why did I design the CommandResultPb this way
                    return new Bytes(resp.getResult().getResult().toByteArray());
                } else {
                    return null;
                }
            case RSQ_NOT_AVAILABLE:
                throw new LHConnectionError(
                    null,
                    "Network error: " + resp.getMessage()
                );
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Not possible.");
        }
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

    private LHROStoreWrapper getGlobalStore() {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
            ServerTopology.GLOBAL_STORE,
            QueryableStoreTypes.keyValueStore()
        );
        ReadOnlyKeyValueStore<String, Bytes> rawGStore = coreStreams.store(params);
        return new LHROStoreWrapper(rawGStore, config);
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

    private Bytes queryRemote(KeyQueryMetadata meta, CentralStoreSubQueryPb subQuery)
        throws LHConnectionError {
        LHInternalsBlockingStub client = getInternalClient(meta.activeHost());
        Exception caught = null;

        try {
            CentralStoreQueryReplyPb resp = client.centralStoreQuery(
                CentralStoreQueryPb
                    .newBuilder()
                    .setEnableStaleStores(false)
                    .setSpecificPartition(meta.partition())
                    .setQuery(subQuery)
                    .build()
            );

            if (resp.getCode() == StoreQueryStatusPb.RSQ_OK) {
                return new Bytes(resp.getResult().toByteArray());
            } else if (resp.getCode() == StoreQueryStatusPb.RSQ_NOT_AVAILABLE) {
                caught = new LHConnectionError(null, "Could not access store.");
            }
        } catch (Exception exn) {
            // It's probably a runtime exception. TODO: investigate grpc error
            // throwing, cuz it's not cool dawg
            caught = exn;
        }

        CentralStoreQueryReplyPb resp = null;
        for (HostInfo standbyHost : meta.standbyHosts()) {
            client = getInternalClient(standbyHost);
            try {
                CentralStoreQueryReplyPb standbyCandidate = client.centralStoreQuery(
                    CentralStoreQueryPb
                        .newBuilder()
                        .setEnableStaleStores(true)
                        .setSpecificPartition(meta.partition())
                        .setQuery(subQuery)
                        .build()
                );

                if (standbyCandidate.getCode() == StoreQueryStatusPb.RSQ_OK) {
                    if (
                        resp == null ||
                        standbyCandidate.getApproximateLag() <
                        resp.getApproximateLag()
                    ) {
                        resp = standbyCandidate;
                    }
                }
            } catch (Exception exn) {
                // If we fail to contact a standby host, just ignore it and
                // proceed to the next standby. If all standby's failed, we still
                // have saved the caught Exception from calling the active host.
                // We will return that original error wrapped in an
                // LHConnectionError.
            }
        }

        if (resp != null) {
            return new Bytes(resp.getResult().toByteArray());
        } else {
            throw new LHConnectionError(
                caught,
                "Failed to look up desired data from active or standby replicas."
            );
        }
    }

    private LHInternalsBlockingStub getInternalClient(HostInfo host) {
        return LHInternalsGrpc.newBlockingStub(getChannel(host));
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
        public void waitForCommandResult(
            WaitForCommandResultPb req,
            StreamObserver<WaitForCommandResultReplyPb> ctx
        ) {
            LHROStoreWrapper store;
            try {
                store = getLocalStore(req.getSpecificPartition(), false);
            } catch (Exception exn) {
                ctx.onNext(
                    WaitForCommandResultReplyPb
                        .newBuilder()
                        .setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE)
                        .setMessage("Failed contacting backend: " + exn.getMessage())
                        .build()
                );
                ctx.onCompleted();
                return;
            }

            int iterations = 0;
            CommandResult result = null;
            while (iterations++ < 500) { // lol
                result = store.get(req.getCommandId(), CommandResult.class);
                if (result == null) {
                    try {
                        Thread.sleep(30);
                    } catch (Exception ignored) {}
                } else {
                    break;
                }
            }

            WaitForCommandResultReplyPb.Builder out = WaitForCommandResultReplyPb.newBuilder();
            out.setCode(StoreQueryStatusPb.RSQ_OK);
            if (result != null) {
                out.setResult(result.toProto());
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

        public void internalPollTask(
            InternalPollTaskPb req,
            StreamObserver<InternalPollTaskReplyPb> ctx
        ) {
            InternalPollTaskReplyPb.Builder out = InternalPollTaskReplyPb.newBuilder();
            try {
                TaskScheduleRequest tsr = findTaskLocally(req.getTaskQueueName());
                if (tsr != null) {
                    out.setResult(tsr.toProto());
                }
                out.setCode(StoreQueryStatusPb.RSQ_OK);
            } catch (LHConnectionError exn) {
                out.setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE);
            }
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
