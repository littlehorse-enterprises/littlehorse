package io.littlehorse.server.streamsimpl;

import com.google.protobuf.ByteString;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCredentials;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.objectId.TaskDefIdModel;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.CentralStoreQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryPb.CentralStoreSubQueryPb;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.InternalGetAdvertisedHostsPb;
import io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb;
import io.littlehorse.common.proto.InternalScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.InternalScanReplyPb;
import io.littlehorse.common.proto.LHInternalsGrpc;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsStub;
import io.littlehorse.common.proto.LocalTasksPb;
import io.littlehorse.common.proto.LocalTasksReplyPb;
import io.littlehorse.common.proto.PartitionBookmarkPb;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.ServerStatePb;
import io.littlehorse.common.proto.ServerStatusPb;
import io.littlehorse.common.proto.StandByTaskStatePb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.TaskStatePb;
import io.littlehorse.common.proto.TopologyInstanceStatePb;
import io.littlehorse.common.proto.TopologyInstanceStateReplyPb;
import io.littlehorse.common.proto.WaitForCommandPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.listener.AdvertisedListenerConfig;
import io.littlehorse.server.streamsimpl.lhinternalscan.InternalScan;
import io.littlehorse.server.streamsimpl.storeinternals.LHROStoreWrapper;
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
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class BackendInternalComms implements Closeable {

    private LHConfig config;
    private Server internalGrpcServer;
    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;
    private HostInfo thisHost;
    private LHProducer producer;

    private ChannelCredentials clientCreds;

    private Map<String, ManagedChannel> channels;
    private AsyncWaiters asyncWaiters;
    private ConcurrentHashMap<HostInfo, InternalGetAdvertisedHostsReplyPb> otherHosts;

    public BackendInternalComms(
        LHConfig config,
        KafkaStreams coreStreams,
        KafkaStreams timerStreams,
        Executor executor
    ) {
        this.config = config;
        this.coreStreams = coreStreams;
        this.timerStreams = timerStreams;
        this.channels = new HashMap<>();
        otherHosts = new ConcurrentHashMap<>();

        ServerBuilder<?> builder;
        clientCreds = config.getInternalClientCreds();

        ServerCredentials security = config.getInternalServerCreds();
        if (security == null) {
            builder = ServerBuilder.forPort(config.getInternalBindPort());
        } else {
            builder =
                Grpc.newServerBuilderForPort(
                    config.getInternalBindPort(),
                    config.getInternalServerCreds()
                );
        }

        internalGrpcServer =
            builder
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(3, TimeUnit.SECONDS)
                .permitKeepAliveTime(10, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .executor(executor)
                .addService(new InterBrokerCommServer())
                .build();

        thisHost =
            new HostInfo(
                config.getInternalAdvertisedHost(),
                config.getInternalAdvertisedPort()
            );
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
    public void getStoreBytesAsync(
        String storeName,
        String fullStoreKey,
        String partitionKey,
        StreamObserver<CentralStoreQueryReplyPb> observer
    ) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            storeName,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            localGetBytesAsync(storeName, fullStoreKey, observer);
        } else {
            queryRemoteAsync(
                meta,
                CentralStoreSubQueryPb.newBuilder().setKey(fullStoreKey).build(),
                observer,
                storeName
            );
        }
    }

    private void localGetBytesAsync(
        String storeName,
        String fullStoreKey,
        StreamObserver<CentralStoreQueryReplyPb> observer
    ) {
        // TODO: We should actually pass in the partition key so that we can
        // improve performance and also safety by getting the store for a specific
        // partition rather than all.
        ReadOnlyKeyValueStore<String, Bytes> store = getRawStore(
            null,
            false,
            storeName
        );
        Bytes result = store.get(fullStoreKey);

        CentralStoreQueryReplyPb.Builder out = CentralStoreQueryReplyPb
            .newBuilder()
            .setCode(StoreQueryStatusPb.RSQ_OK);
        if (result != null) out.setResult(ByteString.copyFrom(result.get()));

        observer.onNext(out.build());
    }

    public void getLastFromPrefixAsync(
        String prefix,
        String partitionKey,
        StreamObserver<CentralStoreQueryReplyPb> observer,
        String storeName
    ) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            storeName,
            partitionKey,
            Serdes.String().serializer()
        );

        if (meta.activeHost().equals(thisHost)) {
            LHROStoreWrapper wrapper = new LHROStoreWrapper(
                getRawStore(null, false, storeName),
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
                observer,
                storeName
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

        /*
         * As a prerequisite to this method being called, the command has already
         * been recorded into the CoreCommand Kafka Topic (and ack'ed by all
         * of the in-sync replicas).
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

    public Set<HostModel> getAllInternalHosts() {
        // It returns a sorted collection always
        return coreStreams
            .metadataForAllStreamsClients()
            .stream()
            .map(meta -> meta.hostInfo())
            .map(hostInfo -> new HostModel(hostInfo.host(), hostInfo.port()))
            .collect(Collectors.toCollection(TreeSet::new));
    }

    public io.littlehorse.sdk.common.proto.HostInfo getAdvertisedHost(
        HostModel host,
        String listenerName
    ) throws LHBadRequestError, LHConnectionError {
        InternalGetAdvertisedHostsReplyPb advertisedHostsForHost = getPublicListenersForHost(
            new HostInfo(host.host, host.port)
        );

        io.littlehorse.sdk.common.proto.HostInfo desiredHost = advertisedHostsForHost.getHostsOrDefault(
            listenerName,
            null
        );
        if (desiredHost == null) {
            String message = String.format(
                """
                Unknown listener name %s. Check LHS_ADVERTISED_LISTENERS on
                LH Server and check the LHW_SERVER_CONNECT_LISTENER config on task worker.
                """,
                listenerName
            );
            throw new LHBadRequestError(message);
        }

        return desiredHost;
    }

    public List<io.littlehorse.sdk.common.proto.HostInfo> getAllAdvertisedHosts(
        String listenerName
    ) throws LHBadRequestError {
        Set<HostModel> hosts = getAllInternalHosts();

        List<io.littlehorse.sdk.common.proto.HostInfo> out = new ArrayList<>();

        for (HostModel host : hosts) {
            try {
                out.add(getAdvertisedHost(host, listenerName));
            } catch (LHConnectionError e) {
                log.warn("Host '{}:{}' unreachable", host.host, host.port);
                // The reason why we don't throw an Exception when the host is unreachable is that, when bootstrapping,
                // other hosts could be in various states of degradation (rebalancing, crashed, running, starting up, etc).
                // Just because one host is down doesn't mean that the entire call to discover the rest of the cluster
                // should fail; the Task Worker should still be able to execute tasks for other LH Server Instances.
                continue;
            }
        }
        return out;
    }

    private InternalGetAdvertisedHostsReplyPb getPublicListenersForHost(
        HostInfo streamsHost
    ) throws LHConnectionError {
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
            throw new LHConnectionError(
                exn,
                String.format(
                    "Host '{}:{}' unreachable",
                    streamsHost.host(),
                    streamsHost.port()
                )
            );
        }
    }

    public LHProducer getProducer() {
        return producer;
    }

    public void onResponseReceived(String commandId, WaitForCommandReplyPb response) {
        asyncWaiters.put(commandId, response);
    }

    public void sendErrorToClientForCommand(String commandId, Exception caught) {
        asyncWaiters.markCommandFailed(commandId, caught);
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

    private ReadOnlyKeyValueStore<String, Bytes> getRawStore(
        Integer specificPartition,
        boolean enableStaleStores,
        String storeName
    ) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
            storeName,
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

    private LHROStoreWrapper getStore(
        Integer specificPartition,
        boolean enableStaleStores,
        String storeName
    ) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(
            specificPartition,
            enableStaleStores,
            storeName
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
        StreamObserver<CentralStoreQueryReplyPb> observer,
        String storeName
    ) {
        // todo
        LHInternalsStub client = getInternalAsyncClient(meta.activeHost());
        client.centralStoreQuery(
            CentralStoreQueryPb
                .newBuilder()
                .setEnableStaleStores(false)
                .setSpecificPartition(meta.partition())
                .setQuery(subQuery)
                .setStore(storeName)
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
            if (clientCreds == null) {
                channel =
                    ManagedChannelBuilder
                        .forAddress(host.host(), host.port())
                        .usePlaintext()
                        .build();
            } else {
                channel =
                    Grpc
                        .newChannelBuilderForAddress(
                            host.host(),
                            host.port(),
                            clientCreds
                        )
                        .build();
            }
            channels.put(key, channel);
        }
        return channel;
    }

    /*
     * Implements the internal_server.proto service, which is used
     * for communication between the LH servers to do distributed lookups etc.
     */
    private class InterBrokerCommServer extends LHInternalsImplBase {

        @Override
        public void topologyInstancesState(
            TopologyInstanceStatePb request,
            StreamObserver<TopologyInstanceStateReplyPb> ctx
        ) {
            var coreServerStates = buildServerStates(coreStreams, "core");
            var timerServerStates = buildServerStates(timerStreams, "timer");

            TopologyInstanceStateReplyPb response = TopologyInstanceStateReplyPb
                .newBuilder()
                .addAllServersCore(coreServerStates)
                .addAllServersTimer(timerServerStates)
                .build();

            ctx.onNext(response);
            ctx.onCompleted();
        }

        private List<ServerStatePb> buildServerStates(
            KafkaStreams kafkaStreams,
            String name
        ) {
            List<ServerStatePb> serverStates = new ArrayList<>();
            kafkaStreams
                .metadataForAllStreamsClients()
                .forEach(streamsClient -> {
                    var hostInfo = streamsClient.hostInfo();
                    var internalClient = getInternalClient(hostInfo);

                    try {
                        LocalTasksReplyPb hostTask = internalClient.localTasks(
                            LocalTasksPb.newBuilder().build()
                        );
                        ServerStatePb serverState = ServerStatePb
                            .newBuilder()
                            .addAllActiveTasks(hostTask.getActiveTasksList())
                            .addAllStandbyTasks(hostTask.getStandbyTasksList())
                            .setHost(hostInfo.host())
                            .setPort(hostInfo.port())
                            .setServerStatus(ServerStatusPb.HOST_UP)
                            .setTopologyName(name)
                            .build();

                        serverStates.add(serverState);
                    } catch (Exception e) {
                        log.warn("Host {} not available to get info", hostInfo);
                        ServerStatePb serverState = ServerStatePb
                            .newBuilder()
                            .addAllActiveTasks(List.of())
                            .addAllStandbyTasks(List.of())
                            .setHost(hostInfo.host())
                            .setPort(hostInfo.port())
                            .setServerStatus(ServerStatusPb.HOST_DOWN)
                            .setTopologyName(name)
                            .setErrorMessage(e.getMessage())
                            .build();

                        serverStates.add(serverState);
                    }
                });
            return serverStates;
        }

        @Override
        public void localTasks(
            LocalTasksPb request,
            StreamObserver<LocalTasksReplyPb> ctx
        ) {
            List<TaskStatePb> activeTasks = coreStreams
                .metadataForLocalThreads()
                .stream()
                .flatMap(threadMetadata -> threadMetadata.activeTasks().stream())
                .flatMap(taskMetadata ->
                    this.buildActiveTasksStatePb(taskMetadata).stream()
                )
                .collect(Collectors.toList());

            List<StandByTaskStatePb> standbyTasks = coreStreams
                .metadataForLocalThreads()
                .stream()
                .flatMap(threadMetadata -> threadMetadata.standbyTasks().stream())
                .flatMap(taskMetadata ->
                    this.buildStandbyTasksStatePb(taskMetadata).stream()
                )
                .collect(Collectors.toList());

            LocalTasksReplyPb response = LocalTasksReplyPb
                .newBuilder()
                .addAllActiveTasks(activeTasks)
                .addAllStandbyTasks(standbyTasks)
                .build();

            ctx.onNext(response);
            ctx.onCompleted();
        }

        private List<TaskStatePb> buildActiveTasksStatePb(TaskMetadata taskMetadata) {
            return taskMetadata
                .topicPartitions()
                .stream()
                .map(topicPartition -> {
                    Long currentOffset = getCurrentOffset(
                        taskMetadata,
                        topicPartition
                    );
                    Long endOffset = getEndOffset(taskMetadata, topicPartition);
                    return TaskStatePb
                        .newBuilder()
                        .setTaskId(taskMetadata.taskId().toString())
                        .setTopic(topicPartition.topic())
                        .setPartition(topicPartition.partition())
                        .setHost(thisHost.host())
                        .setPort(thisHost.port())
                        .setCurrentOffset(currentOffset)
                        .setLag(calculateLag(currentOffset, endOffset))
                        .setRackId(config.getRackId())
                        .build();
                })
                .collect(Collectors.toList());
        }

        private List<StandByTaskStatePb> buildStandbyTasksStatePb(
            TaskMetadata taskMetadata
        ) {
            return taskMetadata
                .topicPartitions()
                .stream()
                .map(topicPartition -> {
                    Long currentOffset = getCurrentOffset(
                        taskMetadata,
                        topicPartition
                    );

                    Long endOffset = getEndOffset(taskMetadata, topicPartition);

                    return StandByTaskStatePb
                        .newBuilder()
                        .setTaskId(taskMetadata.taskId().toString())
                        .setHost(thisHost.host())
                        .setPort(thisHost.port())
                        .setCurrentOffset(currentOffset)
                        .setLag(calculateLag(currentOffset, endOffset))
                        .setRackId(config.getRackId())
                        .build();
                })
                .collect(Collectors.toList());
        }

        private Long calculateLag(Long currentOffset, Long endOffset) {
            if (currentOffset < 0) {
                return endOffset + 1;
            }
            return endOffset - currentOffset;
        }

        private Long getCurrentOffset(
            TaskMetadata taskMetadata,
            TopicPartition topicPartition
        ) {
            return taskMetadata.committedOffsets().containsKey(topicPartition)
                ? taskMetadata.committedOffsets().get(topicPartition)
                : -1;
        }

        private Long getEndOffset(
            TaskMetadata taskMetadata,
            TopicPartition topicPartition
        ) {
            return taskMetadata.endOffsets().containsKey(topicPartition)
                ? taskMetadata.endOffsets().get(topicPartition)
                : -1;
        }

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
                rawStore =
                    getRawStore(
                        specificPartition,
                        req.getEnableStaleStores(),
                        req.getStore()
                    );
            } catch (Exception exn) {
                log.error(exn.getMessage(), exn);
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
        public void internalScan(
            InternalScanPb req,
            StreamObserver<InternalScanReplyPb> ctx
        ) {
            InternalScan lhis = LHSerializable.fromProto(req, InternalScan.class);

            try {
                InternalScanReplyPb reply = doScan(lhis);
                ctx.onNext(reply);
            } catch (LHConnectionError exn) {
                ctx.onNext(
                    InternalScanReplyPb
                        .newBuilder()
                        .setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE)
                        .setMessage("Internal connection error: " + exn.getMessage())
                        .build()
                );
            }

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
            Map<String, io.littlehorse.sdk.common.proto.HostInfo> hosts = config
                .getAdvertisedListeners()
                .stream()
                .collect(
                    Collectors.toMap(
                        AdvertisedListenerConfig::getName,
                        listenerConfig ->
                            io.littlehorse.sdk.common.proto.HostInfo
                                .newBuilder()
                                .setHost(listenerConfig.getHost())
                                .setPort(listenerConfig.getPort())
                                .build()
                    )
                );

            InternalGetAdvertisedHostsReplyPb reply = InternalGetAdvertisedHostsReplyPb
                .newBuilder()
                .putAllHosts(hosts)
                .build();

            ctx.onNext(reply);
            ctx.onCompleted();
        }
    }

    /*
     * EMPLOYEE_TODO: Make this asynchronous rather than blocking.
     *
     * EMPLOYEE_TODO: Failover to Standby replicas if the leader is down.
     */
    public InternalScanReplyPb doScan(InternalScan search) throws LHConnectionError {
        if (
            search.partitionKey != null &&
            search.type == ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN
        ) {
            return objectIdPrefixScan(search);
        } else if (
            search.partitionKey != null && search.type == ScanBoundaryCase.TAG_SCAN
        ) {
            return specificPartitionTagScan(search);
        } else if (
            search.partitionKey == null && search.type == ScanBoundaryCase.TAG_SCAN
        ) {
            return allPartitionTagScan(search);
        } else {
            throw new RuntimeException("Impossible: Unrecognized search type");
        }
    }

    private InternalScanReplyPb specificPartitionTagScan(InternalScan search) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            search.getStoreName(),
            search.getPartitionKey(),
            Serdes.String().serializer()
        );
        InternalScanReplyPb.Builder out = InternalScanReplyPb.newBuilder();
        HostInfo activeHost = meta.activeHost();

        if (activeHost.equals(thisHost)) {
            LHROStoreWrapper store = getStore(
                meta.partition(),
                false,
                search.getStoreName()
            );

            String prefix = search.getTagScan().getKeyPrefix() + "/";
            try (
                LHKeyValueIterator<Tag> tagScanResultIterator = store.prefixScan(
                    prefix,
                    Tag.class
                )
            ) {
                List<ByteString> matchingObjectIds = new ArrayList<>();
                while (tagScanResultIterator.hasNext()) {
                    LHIterKeyValue<Tag> currentItem = tagScanResultIterator.next();
                    Tag matchingTag = currentItem.getValue();
                    ByteString matchingObjectId = ObjectId
                        .fromString(
                            matchingTag.getDescribedObjectId(),
                            Getable.getIdCls(search.getObjectType())
                        )
                        .toProto()
                        .build()
                        .toByteString();
                    matchingObjectIds.add(matchingObjectId);
                    if (matchingObjectIds.size() == search.getLimit()) {
                        break;
                    }
                }
                out.addAllResults(matchingObjectIds);
            }
        } else {
            InternalScanReplyPb reply = getInternalClient(activeHost)
                .internalScan(search.toProto().build());
            out.addAllResults(reply.getResultsList());
        }

        return out.build();
    }

    private InternalScanReplyPb objectIdPrefixScan(InternalScan search)
        throws LHConnectionError {
        HostInfo correctHost = getHostForKey(search.storeName, search.partitionKey);

        if (getHostForKey(search.storeName, search.partitionKey).equals(thisHost)) {
            return objectIdPrefixScanOnThisHost(search);
        } else {
            try {
                return getInternalClient(correctHost)
                    .internalScan(search.toProto().build());
            } catch (Exception exn) {
                // EMPLOYEE_TODO: make the caught exn specific to grpc
                // EMPLOYEE_TODO: use standby hosts
                return InternalScanReplyPb
                    .newBuilder()
                    .setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE)
                    .setMessage("Failed contacting host: " + exn.getMessage())
                    .build();
            }
        }
    }

    private InternalScanReplyPb objectIdPrefixScanOnThisHost(InternalScan req) {
        /*
         * TODO: There's some things we need to verify here.
         * 1) It's a prerequisite that the request has a partition key set.
         */
        int curLimit = req.limit;
        BookmarkPb reqBookmark = req.bookmark;
        if (reqBookmark == null) {
            reqBookmark = BookmarkPb.newBuilder().build();
        }
        InternalScanReplyPb.Builder out = InternalScanReplyPb.newBuilder();

        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            req.storeName,
            req.partitionKey,
            Serdes.String().serializer()
        );
        int partition = meta.partition();

        LHROStoreWrapper store = getStore(partition, false, req.storeName);
        PartitionBookmarkPb partBookmark = reqBookmark.getInProgressPartitionsOrDefault(
            partition,
            null
        );

        String endKey = req.boundedObjectIdScan.getEndObjectId() + "~";
        String startKey;
        if (partBookmark == null) {
            startKey = req.boundedObjectIdScan.getStartObjectId();
        } else {
            startKey = partBookmark.getLastKey();
        }
        String bookmarkKey = null;
        boolean brokenBecauseOutOfData = true;

        try (
            LHKeyValueIterator<? extends Storeable<?>> iter = store.range(
                startKey,
                endKey,
                Getable.getCls(req.objectType)
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<? extends Storeable<?>> next = iter.next();
                if (--curLimit < 0) {
                    bookmarkKey = next.getKey();
                    brokenBecauseOutOfData = false;
                    break;
                }
                out.addResults(
                    iterKeyValueToInternalScanResult(
                        next,
                        req.resultType,
                        req.objectType
                    )
                );
            }
        }

        if (!brokenBecauseOutOfData) {
            // Then we have more data for the next request, so we want to return
            // a bookmark.

            PartitionBookmarkPb nextBookmark = PartitionBookmarkPb
                .newBuilder()
                .setParttion(partition)
                .setLastKey(bookmarkKey)
                .build();

            out.setUpdatedBookmark(
                BookmarkPb
                    .newBuilder()
                    .putInProgressPartitions(partition, nextBookmark)
            );
        } else {
            // If we never set `bookmarkKey`, then we know that we read all of the
            // data. So we don't set a bookmark on the response.
            if (bookmarkKey != null) {
                throw new RuntimeException("not possible");
            }
        }

        curLimit -= out.getResultsCount();

        return out.build();
    }

    private ByteString iterKeyValueToInternalScanResult(
        LHIterKeyValue<? extends Storeable<?>> next,
        ScanResultTypePb resultType,
        GetableClassEnum objectType
    ) {
        if (resultType == ScanResultTypePb.OBJECT) {
            return ByteString.copyFrom(next.getValue().toBytes(config));
        } else if (resultType == ScanResultTypePb.OBJECT_ID) {
            Class<? extends ObjectId<?, ?, ?>> idCls = Getable.getIdCls(objectType);

            return ByteString.copyFrom(
                ObjectId.fromString(next.getKey(), idCls).toBytes(config)
            );
        } else {
            throw new RuntimeException("Impossible: unknown result type");
        }
    }

    private InternalScanReplyPb allPartitionTagScan(InternalScan search)
        throws LHConnectionError {
        int limit = search.limit;

        // First, see what results we have locally. Then if we need more results
        // to hit the limit, we query another host.
        // How do we know which host to query? Well, we find a partition which
        // hasn't been completed yet (by consulting the Bookmark), and then
        // query the owner of that partition.

        InternalScanReplyPb out = localAllPartitionTagScan(search);
        if (out.getResultsCount() >= limit) {
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
        while (out.hasUpdatedBookmark() && out.getResultsCount() < search.limit) {
            HostInfo otherHost = getHostForPartition(
                getRandomUnfinishedPartition(out.getUpdatedBookmark())
            );
            if (otherHost.equals(thisHost)) {
                throw new RuntimeException("wtf, host the same");
            }
            LHInternalsBlockingStub stub = getInternalClient(otherHost);

            InternalScan newReq = new InternalScan();
            newReq.bookmark = out.getUpdatedBookmark();
            newReq.limit = search.limit - out.getResultsCount();
            newReq.tagScan = search.tagScan;
            newReq.type = ScanBoundaryCase.TAG_SCAN;
            newReq.objectType = search.objectType;
            newReq.storeName = search.storeName;
            newReq.resultType = ScanResultTypePb.OBJECT_ID;

            InternalScanReplyPb reply;
            try {
                reply = stub.internalScan(newReq.toProto().build());
            } catch (Exception exn) {
                throw new LHConnectionError(exn, "Failed connecting to backend.");
            }
            if (reply.getCode() != StoreQueryStatusPb.RSQ_OK) {
                throw new LHConnectionError(null, "Failed connecting to backend.");
            }
            InternalScanReplyPb.Builder newOutBuilder = InternalScanReplyPb
                .newBuilder()
                .addAllResults(out.getResultsList())
                .addAllResults(reply.getResultsList());

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
            throw new RuntimeException("Unrecognized partition");
        }

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

    private HostInfo getHostForKey(String storeName, String partitionKey) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
            storeName,
            partitionKey,
            Serdes.String().serializer()
        );
        return meta.activeHost();
    }

    /*
     * Precondition: bm represents bookmark for uncompleted LOCAL search (not a
     * REMOTE_HASH search).
     */
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

    public LHGlobalMetaStores getGlobalStoreImpl() {
        return new GlobalMetaStoresServerImpl(coreStreams, config);
    }

    private InternalScanReplyPb localAllPartitionTagScan(InternalScan req) {
        log.debug("Local Tag prefix scan");
        if (req.partitionKey != null) {
            throw new IllegalArgumentException(
                "called localAllPartitionTagScan with partitionKey"
            );
        }

        int curLimit = req.limit;

        BookmarkPb reqBookmark = req.bookmark;
        if (reqBookmark == null) {
            reqBookmark = BookmarkPb.newBuilder().build();
        }

        BookmarkPb.Builder outBookmark = reqBookmark.toBuilder();
        InternalScanReplyPb.Builder out = InternalScanReplyPb.newBuilder();

        // iterate through all active and standby local partitions
        for (int partition : getLocalActiveCommandProcessorPartitions()) {
            LHROStoreWrapper partStore = getStore(partition, false, req.storeName);
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
            Pair<List<ByteString>, PartitionBookmarkPb> result = onePartitionPaginatedTagScan(
                req.tagScan,
                partBookmark,
                curLimit,
                req.objectType,
                partition,
                partStore
            );

            curLimit -= result.getLeft().size();
            out.addAllResults(result.getLeft());
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

    /*
     * Tag Scans only return object id's. That's because `Tag`s function as a
     * secondary index.
     */
    private Pair<List<ByteString>, PartitionBookmarkPb> onePartitionPaginatedTagScan(
        TagScanPb tagPrefixScan,
        PartitionBookmarkPb bookmark,
        int limit,
        GetableClassEnum objectType,
        int partition,
        LHROStoreWrapper store
    ) {
        PartitionBookmarkPb bookmarkOut = null;
        List<ByteString> idsOut = new ArrayList<>();

        String startKey;
        String endKey;

        if (bookmark == null) {
            startKey = tagPrefixScan.getKeyPrefix() + "/";
            if (tagPrefixScan.hasEarliestCreateTime()) {
                startKey +=
                    LHUtil.toLhDbFormat(
                        LHUtil.fromProtoTs(tagPrefixScan.getEarliestCreateTime())
                    ) +
                    "/";
            }
        } else {
            startKey = bookmark.getLastKey();
        }

        endKey = tagPrefixScan.getKeyPrefix() + "/";
        if (tagPrefixScan.hasLatestCreateTime()) {
            endKey +=
                LHUtil.toLhDbFormat(
                    LHUtil.fromProtoTs(tagPrefixScan.getLatestCreateTime())
                ) +
                "/";
        }
        endKey += "~";

        try (
            LHKeyValueIterator<Tag> iter = store.range(startKey, endKey, Tag.class)
        ) {
            boolean brokenBecauseOutOfData = true;
            while (iter.hasNext()) {
                LHIterKeyValue<Tag> next = iter.next();
                Tag tag = next.getValue();
                if (--limit < 0) {
                    bookmarkOut =
                        PartitionBookmarkPb
                            .newBuilder()
                            .setParttion(partition)
                            .setLastKey(tag.getStoreKey())
                            .build();

                    // broke loop because we filled up the limit
                    brokenBecauseOutOfData = false;
                    break;
                }

                // Turn the ID String into the ObjectId structure, then serialize it
                // to proto
                Class<? extends ObjectId<?, ?, ?>> idCls = Getable.getIdCls(
                    objectType
                );
                idsOut.add(
                    ObjectId
                        .fromString(next.getValue().describedObjectId, idCls)
                        .toProto()
                        .build()
                        .toByteString()
                );
            }

            if (brokenBecauseOutOfData) {
                bookmarkOut = null;
            }
        }
        return Pair.of(idsOut, bookmarkOut);
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

class GlobalMetaStoresServerImpl implements LHGlobalMetaStores {

    private LHROStoreWrapper store;

    public GlobalMetaStoresServerImpl(KafkaStreams coreStreams, LHConfig config) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params = StoreQueryParameters.fromNameAndType(
            ServerTopology.GLOBAL_STORE,
            QueryableStoreTypes.keyValueStore()
        );

        store = new LHROStoreWrapper(coreStreams.store(params), config);
    }

    public WfSpecModel getWfSpec(String name, Integer version) {
        if (version != null) {
            return store.get(
                new WfSpecIdModel(name, version).toString(),
                WfSpecModel.class
            );
        } else {
            return store.getLastFromPrefix(name, WfSpecModel.class);
        }
    }

    public TaskDefModel getTaskDef(String name) {
        return store.get(new TaskDefIdModel(name).toString(), TaskDefModel.class);
    }

    public ExternalEventDefModel getExternalEventDef(String name) {
        return store.get(
            new ExternalEventDefIdModel(name).toString(),
            ExternalEventDefModel.class
        );
    }

    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        return (
            store.get(
                new UserTaskDefIdModel(name, version).getStoreKey(),
                UserTaskDefModel.class
            )
        );
    }
}
