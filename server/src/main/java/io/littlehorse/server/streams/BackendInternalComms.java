package io.littlehorse.server.streams;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.ChannelCredentials;
import io.grpc.Context;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCredentials;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.proto.*;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsStub;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.auth.InternalAuthorizer;
import io.littlehorse.server.auth.InternalCallCredentials;
import io.littlehorse.server.listener.AdvertisedListenerConfig;
import io.littlehorse.server.streams.lhinternalscan.InternalScanRequestModel;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class BackendInternalComms implements Closeable {

    private LHServerConfig config;
    private Server internalGrpcServer;
    private KafkaStreams coreStreams;

    @Getter
    private HostInfo thisHost;

    private LHProducer producer;

    private ChannelCredentials clientCreds;

    private Map<String, ManagedChannel> channels;
    private AsyncWaiters asyncWaiters;
    private ConcurrentHashMap<HostInfo, InternalGetAdvertisedHostsResponse> otherHosts;

    private final Context.Key<RequestExecutionContext> contextKey;

    public BackendInternalComms(
            LHServerConfig config,
            KafkaStreams coreStreams,
            KafkaStreams timerStreams,
            Executor executor,
            MetadataCache metadataCache,
            Context.Key<RequestExecutionContext> contextKey,
            BiFunction<Integer, String, ReadOnlyKeyValueStore<String, Bytes>> storeProvider) {
        this.config = config;
        this.coreStreams = coreStreams;
        this.channels = new HashMap<>();
        this.contextKey = contextKey;
        otherHosts = new ConcurrentHashMap<>();

        ServerBuilder<?> builder;
        clientCreds = config.getInternalClientCreds();

        ServerCredentials security = config.getInternalServerCreds();
        if (security == null) {
            builder = ServerBuilder.forPort(config.getInternalBindPort());
        } else {
            builder = Grpc.newServerBuilderForPort(config.getInternalBindPort(), config.getInternalServerCreds());
        }

        internalGrpcServer = builder.keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(3, TimeUnit.SECONDS)
                .permitKeepAliveTime(10, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .executor(executor)
                .addService(new InterBrokerCommServer())
                .intercept(new InternalAuthorizer(contextKey, storeProvider, metadataCache, config))
                .build();

        thisHost = new HostInfo(config.getInternalAdvertisedHost(), config.getInternalAdvertisedPort());
        this.producer = config.getProducer();
        this.asyncWaiters = new AsyncWaiters();

        // TODO: Optimize this later.
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

    public RequestExecutionContext executionContext() {
        return contextKey.get();
    }

    public void close() {
        log.info("Closing backend internal comms");
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

    public <U extends Message, T extends AbstractGetable<U>> T getObject(
            ObjectIdModel<?, U, T> objectId, Class<T> clazz, ExecutionContext context) throws LHSerdeError {

        if (objectId.getPartitionKey().isEmpty()) {
            throw new IllegalArgumentException(
                    "Can't get object without partition key; metadata objects have their own store");
        }

        String storeName = objectId.getStore().getStoreName();

        KeyQueryMetadata metadata = coreStreams.queryMetadataForKey(
                storeName, objectId.getPartitionKey().get(), Serdes.String().serializer());

        if (metadata.activeHost().equals(thisHost)) {
            return getObjectLocal(objectId, clazz, metadata.partition());
        } else {
            return LHSerializable.fromBytes(
                    getInternalClient(metadata.activeHost())
                            .getObject(GetObjectRequest.newBuilder()
                                    .setObjectType(objectId.getType())
                                    .setObjectId(objectId.toString())
                                    .setPartition(metadata.partition())
                                    .build())
                            .getResponse()
                            .toByteArray(),
                    clazz,
                    context);
        }
    }

    public void waitForCommand(AbstractCommand<?> command, StreamObserver<WaitForCommandResponse> observer) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
                ServerTopology.CORE_STORE,
                command.getPartitionKey(),
                Serdes.String().serializer());

        /*
         * As a prerequisite to this method being called, the command has already
         * been recorded into the CoreCommand Kafka Topic (and ack'ed by all
         * of the in-sync replicas).
         */
        if (meta.activeHost().equals(thisHost)) {
            localWaitForCommand(command.getCommandId(), observer);
        } else {
            WaitForCommandRequest req = WaitForCommandRequest.newBuilder()
                    .setCommandId(command.getCommandId())
                    .build();
            getInternalAsyncClient(meta.activeHost()).waitForCommand(req, observer);
        }
    }

    public Set<HostModel> getAllInternalHosts() {
        // It returns a sorted collection always
        return coreStreams.metadataForAllStreamsClients().stream()
                .map(StreamsMetadata::hostInfo)
                .map(hostInfo -> new HostModel(hostInfo.host(), hostInfo.port()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public LHHostInfo getAdvertisedHost(
            HostModel host, String listenerName, InternalCallCredentials internalCredentials) {
        InternalGetAdvertisedHostsResponse advertisedHostsForHost =
                getPublicListenersForHost(new HostInfo(host.host, host.port), internalCredentials);

        LHHostInfo desiredHost = advertisedHostsForHost.getHostsOrDefault(listenerName, null);
        if (desiredHost == null) {
            String message = String.format(
                    """
                            Unknown listener name %s. Check LHS_ADVERTISED_LISTENERS on
                            LH Server and check the LHW_SERVER_CONNECT_LISTENER config on task worker.
                            """,
                    listenerName);
            throw new LHApiException(Status.INVALID_ARGUMENT, message);
        }

        return desiredHost;
    }

    public List<LHHostInfo> getAllAdvertisedHosts(String listenerName) throws LHBadRequestError {
        Set<HostModel> hosts = getAllInternalHosts();

        List<LHHostInfo> out = new ArrayList<>();

        for (HostModel host : hosts) {
            try {
                // Potential NPE if this method gets invoked, currently is not used
                out.add(getAdvertisedHost(host, listenerName, null));
            } catch (StatusRuntimeException exn) {
                log.warn("Host '{}:{}' unreachable: ", host.host, host.port, exn);
                // The reason why we don'swallow the exception when one host is unreachable is
                // that, when bootstrapping, other hosts could be in various states of degradation
                // (rebalancing, crashed, running, starting up, etc).
                // Just because one host is down doesn't mean that the entire call to discover
                // the rest of the cluster should fail; the Task Worker should still be able to
                // execute tasks for other LH Server Instances.
                continue;
            }
        }
        return out;
    }

    private InternalGetAdvertisedHostsResponse getPublicListenersForHost(
            HostInfo streamsHost, InternalCallCredentials internalCredentials) {
        if (otherHosts.get(streamsHost) != null) {
            return otherHosts.get(streamsHost);
        }

        InternalGetAdvertisedHostsResponse info =
                getInternalClient(streamsHost, internalCredentials).getAdvertisedHosts(Empty.getDefaultInstance());

        otherHosts.put(streamsHost, info);
        return info;
    }

    public LHProducer getProducer() {
        return producer;
    }

    public void onResponseReceived(String commandId, WaitForCommandResponse response) {
        asyncWaiters.put(commandId, response);
    }

    public void sendErrorToClientForCommand(String commandId, Exception caught) {
        asyncWaiters.markCommandFailed(commandId, caught);
    }

    private void localWaitForCommand(String commandId, StreamObserver<WaitForCommandResponse> observer) {
        asyncWaiters.put(commandId, observer);
        // Once the command has been recorded, we've got nothing to do: the
        // CommandProcessor will notify the StreamObserver once the command is
        // processed.
    }

    public ReadOnlyKeyValueStore<String, Bytes> getRawStore(
            int specificPartition, String storeName) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

                params = params.withPartition(specificPartition);

        return coreStreams.store(params);
    }

    private ReadOnlyTenantScopedStore getStore(int specificPartition, String storeName) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(specificPartition, storeName);
        RequestExecutionContext requestContext = executionContext();
        AuthorizationContext authContext = requestContext.authorization();
        return ReadOnlyTenantScopedStore.newInstance(rawStore, authContext.tenantId(), requestContext);
    }

    public LHInternalsBlockingStub getInternalClient(HostInfo host, InternalCallCredentials internalCredentials) {
        return LHInternalsGrpc.newBlockingStub(getChannel(host)).withCallCredentials(internalCredentials);
    }

    public LHInternalsBlockingStub getInternalClient(HostInfo host) {
        return getInternalClient(host, InternalCallCredentials.forContext(executionContext()));
    }

    private LHInternalsStub getInternalAsyncClient(HostInfo host) {
        return LHInternalsGrpc.newStub(getChannel(host))
                .withCallCredentials(InternalCallCredentials.forContext(new BackgroundContext()));
    }

    private ManagedChannel getChannel(HostInfo host) {
        String key = host.host() + ":" + host.port();
        ManagedChannel channel = channels.get(key);
        if (channel == null) {
            if (clientCreds == null) {
                channel = ManagedChannelBuilder.forAddress(host.host(), host.port())
                        .usePlaintext()
                        .build();
            } else {
                channel = Grpc.newChannelBuilderForAddress(host.host(), host.port(), clientCreds)
                        .build();
            }
            channels.put(key, channel);
        }
        return channel;
    }

    @SuppressWarnings("unchecked")
    private <U extends Message, T extends AbstractGetable<U>> T getObjectLocal(
            ObjectIdModel<?, U, T> objectId, Class<T> clazz, int partition) {

        ReadOnlyTenantScopedStore store =
                getStore(partition, objectId.getStore().getStoreName());
        StoredGetable<U, T> storeResult =
                (StoredGetable<U, T>) store.get(objectId.getStoreableKey(), StoredGetable.class);
        if (storeResult == null) {
            throw new LHApiException(Status.NOT_FOUND, "Requested object was not found.");
        }

        return storeResult.getStoredObject();
    }

    /*
     * Implements the internal_server.proto service, which is used
     * for communication between the LH servers to do distributed lookups etc.
     */
    private class InterBrokerCommServer extends LHInternalsImplBase {

        @Override
        public void getObject(GetObjectRequest request, StreamObserver<GetObjectResponse> observer) {
            ObjectIdModel<?, ?, ?> id =
                    ObjectIdModel.fromString(request.getObjectId(), AbstractGetable.getIdCls(request.getObjectType()));

            String storeName = id.getStore().getStoreName();
            ReadOnlyTenantScopedStore store = getStore(request.getPartition(), storeName);

            @SuppressWarnings("unchecked")
            StoredGetable<?, ?> entity = store.get(id.getStoreableKey(), StoredGetable.class);

            if (entity == null) {
                observer.onError(new LHApiException(Status.NOT_FOUND, "Requested object was not found."));
            } else {
                observer.onNext(GetObjectResponse.newBuilder()
                        .setResponse(entity.getStoredObject().toProto().build().toByteString())
                        .build());
                observer.onCompleted();
            }
        }

        @Override
        public void internalScan(InternalScanRequest req, StreamObserver<InternalScanResponse> observer) {
            InternalScanRequestModel lhis =
                    LHSerializable.fromProto(req, InternalScanRequestModel.class, executionContext());
            try {
                InternalScanResponse reply = doScan(lhis);
                observer.onNext(reply);
                observer.onCompleted();
            } catch (LHApiException exn) {
                observer.onError(exn);
            } catch (Exception exn) {
                observer.onError(new LHApiException(Status.UNKNOWN, exn));
            }
        }

        @Override
        public void waitForCommand(WaitForCommandRequest req, StreamObserver<WaitForCommandResponse> ctx) {
            localWaitForCommand(req.getCommandId(), ctx);
        }

        @Override
        public void getAdvertisedHosts(Empty req, StreamObserver<InternalGetAdvertisedHostsResponse> ctx) {
            Map<String, io.littlehorse.sdk.common.proto.LHHostInfo> hosts = config.getAdvertisedListeners().stream()
                    .collect(Collectors.toMap(
                            AdvertisedListenerConfig::getName,
                            listenerConfig -> io.littlehorse.sdk.common.proto.LHHostInfo.newBuilder()
                                    .setHost(listenerConfig.getHost())
                                    .setPort(listenerConfig.getPort())
                                    .build()));

            InternalGetAdvertisedHostsResponse reply = InternalGetAdvertisedHostsResponse.newBuilder()
                    .putAllHosts(hosts)
                    .build();

            ctx.onNext(reply);
            ctx.onCompleted();
        }
    }

    public InternalScanResponse doScan(InternalScanRequestModel search) {
        // First, figure out which partitions need to be addressed.
        Set<Integer> unfinishedPartitions = search.getIncompletePartitions(config);

        Set<Integer> localUnfinishedPartitions = unfinishedPartitions.stream()
                .filter(partition -> isLocalPartition(partition, search.getStoreName()))
                .collect(Collectors.toSet());

        // These are the hosts that have unfinished data.
        Set<HostInfo> remoteHostsWithData = unfinishedPartitions.stream()
                .filter(partition -> !localUnfinishedPartitions.contains(partition))
                .map(partition -> getHostForPartition(partition, search.getStoreName()))
                .collect(Collectors.toSet());

        // Address all of the local partitions first. Once those have been addressed,
        // pass the request along and search the remote partitions on the other hosts.
        Iterator<Integer> iter = localUnfinishedPartitions.iterator();
        InternalScanResponse.Builder response = InternalScanResponse.newBuilder();

        while (iter.hasNext() && response.getResultsCount() < search.getLimit()) {
            int partition = iter.next();
            search.scanPartition(executionContext(), partition, response);
        }

        // Next: Scan the remote partitions
        Iterator<HostInfo> otherHosts = remoteHostsWithData.iterator();
        while (otherHosts.hasNext() && response.getResultsCount() < search.getLimit()) {
            HostInfo otherHost = otherHosts.next();
            scanRemoteHost(search, otherHost, response);
        }

        if (isNothingLeft(response.getUpdatedBookmark(), search.getStoreName())) response.clearUpdatedBookmark();

        return response.build();
    }

    /**
     * Determines whether a bookmark denotes a searc that is already completed.
     * @param bookmark is a bookmark
     * @param storeName is the store that we're searching in
     * @return true if we've finished the search.
     */
    private boolean isNothingLeft(BookmarkPb bookmark, String storeName) {
        int numPartitions = storeName.equals(ServerTopology.METADATA_STORE) ? 1 : config.getClusterPartitions();
        return bookmark.getCompletedPartitionsCount() == numPartitions;
    }

    /**
     * Scans a single partition and returns the results, and an optional String denoting
     * the next key to resume from in case of an incomplete scan.
     * @param query is the query to execute over the partition.
     * @param partition is the partition to scan.
     * @return a pair of results, nextKey
     */
    private void scanLocalPartition(
            InternalScanRequestModel query, int partition, InternalScanResponse.Builder response) {
        
        query.scanPartition(executionContext(), partition, response);
    }

    private void scanRemoteHost(
            InternalScanRequestModel search, HostInfo remoteHost, InternalScanResponse.Builder response) {

        InternalScanRequest.Builder requestBuilder = search.toProto();
        requestBuilder.setBookmark(response.getUpdatedBookmark());
        requestBuilder.setLimit(search.getLimit() - response.getResultsCount());

        InternalScanResponse remoteResponse = getInternalClient(remoteHost).internalScan(requestBuilder.build());
        response.addAllResults(remoteResponse.getResultsList());

        response.getUpdatedBookmarkBuilder()
                .putAllInProgressPartitions(remoteResponse.getUpdatedBookmark().getInProgressPartitionsMap());

        response.getUpdatedBookmarkBuilder()
                .clearCompletedPartitions()
                .addAllCompletedPartitions(remoteResponse.getUpdatedBookmark().getCompletedPartitionsList());
    }

    private boolean isLocalPartition(int partition, String storeName) {
        if (storeName.equals(ServerTopology.METADATA_STORE)) {
            // Every host has the metadata store since it's a global store.
            return true;
        } else if (storeName.equals(ServerTopology.CORE_STORE)) {
            return getLocalActiveCommandProcessorPartitions().contains(partition);
        } else if (storeName.equals(ServerTopology.CORE_REPARTITION_STORE)) {
            return getLocalActiveRepartitionedPartitions().contains(partition);
        }
        throw new IllegalArgumentException("Unrecognized store name %s".formatted(storeName));
    }

    private HostInfo getHostForPartition(int partition, String storeName) {
        if (partition >= config.getClusterPartitions()) {
            throw new LHMisconfigurationException("Unrecognized partition");
        }
        if (!storeName.equals(ServerTopology.CORE_STORE) && !storeName.equals(ServerTopology.CORE_REPARTITION_STORE)) {
            throw new IllegalArgumentException("Invalid storename %s".formatted(storeName));
        }

        Predicate<TopicPartition> isForCorrectHost = storeName.equals(ServerTopology.CORE_STORE)
                ? (tp) -> isCommandProcessor(tp, config)
                : (tp) -> isRepartitionedPartition(tp, config);

        Collection<StreamsMetadata> all = coreStreams.metadataForAllStreamsClients();
        for (StreamsMetadata meta : all) {
            for (TopicPartition tp : meta.topicPartitions()) {
                if (tp.partition() != partition) continue;
                if (isForCorrectHost.test(tp)) {
                    return meta.hostInfo();
                }
            }
        }
        throw new LHApiException(
                Status.UNAVAILABLE, "Streams application is in %s state".formatted(coreStreams.state()));
    }

    private Set<Integer> getLocalActiveCommandProcessorPartitions() {
        Set<Integer> out = new HashSet<>();

        for (ThreadMetadata thread : coreStreams.metadataForLocalThreads()) {
            for (TaskMetadata activeTask : thread.activeTasks()) {
                // We only want to query active partitions.
                // Additionally, there may be many tasks...we only want the ones
                // that are for the command processor.
                if (isCommandProcessor(activeTask, config)) {
                    out.add(activeTask.taskId().partition());
                }
            }
        }
        return out;
    }

    private Set<Integer> getLocalActiveRepartitionedPartitions() {
        Set<Integer> out = new HashSet<>();

        for (ThreadMetadata thread : coreStreams.metadataForLocalThreads()) {
            for (TaskMetadata activeTask : thread.activeTasks()) {
                // We only want to query active partitions.
                // Additionally, there may be many tasks...we only want the ones
                // that are for the command processor.
                if (isRepartitionedPartition(activeTask, config)) {
                    out.add(activeTask.taskId().partition());
                }
            }
        }
        return out;
    }

    private static boolean isRepartitionedPartition(TaskMetadata task, LHServerConfig config) {
        for (TopicPartition tPart : task.topicPartitions()) {
            if (isRepartitionedPartition(tPart, config)) return true;
        }
        return false;
    }

    private static boolean isCommandProcessor(TaskMetadata task, LHServerConfig config) {
        for (TopicPartition tPart : task.topicPartitions()) {
            if (isCommandProcessor(tPart, config)) return true;
        }
        return false;
    }

    private static boolean isCommandProcessor(TopicPartition tPart, LHServerConfig config) {
        return tPart.topic().equals(config.getCoreCmdTopicName());
    }

    private static boolean isRepartitionedPartition(TopicPartition tPart, LHServerConfig config) {
        return tPart.topic().equals(config.getRepartitionTopicName());
    }
}
