package io.littlehorse.server.streams;

import com.google.protobuf.ByteString;
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
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.*;
import io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb;
import io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase;
import io.littlehorse.common.proto.InternalScanPb.TagScanPb;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsImplBase;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsStub;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.auth.InternalAuthorizer;
import io.littlehorse.server.auth.InternalCallCredentials;
import io.littlehorse.server.listener.AdvertisedListenerConfig;
import io.littlehorse.server.streams.lhinternalscan.InternalScan;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.scanfilter.ScanFilterModel;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
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
    private final Pattern objectIdExtractorPattern = Pattern.compile("[0-9]+/[0-9]+/");

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

    public List<io.littlehorse.sdk.common.proto.LHHostInfo> getAllAdvertisedHosts(String listenerName)
            throws LHBadRequestError {
        Set<HostModel> hosts = getAllInternalHosts();

        List<io.littlehorse.sdk.common.proto.LHHostInfo> out = new ArrayList<>();

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
            Integer specificPartition, boolean enableStaleStores, String storeName) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

        if (enableStaleStores) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        return coreStreams.store(params);
    }

    private ReadOnlyTenantScopedStore getStore(Integer specificPartition, boolean enableStaleStores, String storeName) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(specificPartition, enableStaleStores, storeName);
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
                getStore(partition, false, objectId.getStore().getStoreName());
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
            ReadOnlyTenantScopedStore store = getStore(request.getPartition(), false, storeName);

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
        public void internalScan(InternalScanPb req, StreamObserver<InternalScanResponse> observer) {
            InternalScan lhis = LHSerializable.fromProto(req, InternalScan.class, executionContext());
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

    /*
     * EMPLOYEE_TODO: Make this asynchronous rather than blocking.
     *
     * EMPLOYEE_TODO: Failover to Standby replicas if the leader is down.
     */
    public InternalScanResponse doScan(InternalScan search) {
        if (search.getStoreName().equals(ServerTopology.GLOBAL_METADATA_STORE)) {
            // This will be cleaned up in a two-part refactor of the stores. The first part is
            // in #556.
            return doGlobalStoreScan(search);
        }

        if (search.partitionKey != null && search.type == ScanBoundaryCase.BOUNDED_OBJECT_ID_SCAN) {
            return objectIdPrefixScan(search);
        } else if (search.partitionKey != null && search.type == ScanBoundaryCase.TAG_SCAN) {
            return specificPartitionTagScan(search);
        } else if (search.partitionKey == null && search.type == ScanBoundaryCase.TAG_SCAN) {
            return allPartitionTagScan(search);
        } else {
            throw new RuntimeException("Impossible: Unrecognized search type");
        }
    }

    // TODO: Remove this in part-2 of the #556 refactor
    private InternalScanResponse doGlobalStoreScan(InternalScan search) {
        // Note that there is only one partition in the global store, because, well, it's global
        int partition = 0;

        // This is also gross.
        if (!search.getStoreName().equals(ServerTopology.GLOBAL_METADATA_STORE)) {
            throw new IllegalStateException("Tried to do a global store scan on non-global store search");
        }

        PartitionBookmarkPb partBookmark = null;
        if (search.getBookmark() != null) {
            if (search.getBookmark().getCompletedPartitionsCount() > 0) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "Search request provided corrupted bookmark.");
            }
            partBookmark = search.getBookmark().getInProgressPartitionsOrDefault(partition, null);
        }

        Pair<List<ByteString>, PartitionBookmarkPb> result;
        if (search.getTagScan() != null) {
            result = onePartitionPaginatedTagScan(
                    search.tagScan,
                    partBookmark,
                    search.getLimit(),
                    search.getObjectType(),
                    partition,
                    search.getFilters(),
                    search.getStoreName());
        } else {
            result = objectIdPrefixScanGlobalStore(
                    search.boundedObjectIdScan, partBookmark, search.getLimit(), search.getObjectType());
        }

        InternalScanResponse.Builder out = InternalScanResponse.newBuilder().addAllResults(result.getLeft());
        if (result.getRight() != null) {
            out.setUpdatedBookmark(BookmarkPb.newBuilder().putInProgressPartitions(partition, result.getRight()));
        }
        return out.build();
    }

    // This will be removed during the refactor, as it is totally gross.
    private Pair<List<ByteString>, PartitionBookmarkPb> objectIdPrefixScanGlobalStore(
            BoundedObjectIdScanPb objectIdScan, PartitionBookmarkPb bookmark, int limit, GetableClassEnum objectType) {

        String endKey = StoredGetable.getRocksDBKey(objectIdScan.getEndObjectId() + "~", objectType);
        String startKey;
        if (bookmark == null) {
            startKey = StoredGetable.getRocksDBKey(objectIdScan.getStartObjectId(), objectType);
        } else {
            startKey = bookmark.getLastKey();
        }

        String bookmarkKey = null;
        List<ByteString> results = new ArrayList<>();

        try (LHKeyValueIterator<?> iter = createObjectIdIteratorGlobalStore(
                startKey, endKey, objectType, ServerTopology.GLOBAL_METADATA_STORE, 0)) {

            while (iter.hasNext()) {
                LHIterKeyValue<? extends Storeable<?>> next = iter.next();
                if (--limit < 0) {
                    bookmarkKey = next.getValue().getStoreKey();
                    break;
                }
                results.add(iterKeyValueToInternalScanResult(next, ScanResultTypePb.OBJECT_ID, objectType));
            }
        }
        PartitionBookmarkPb bookmarkOut = bookmarkKey == null
                ? null
                : PartitionBookmarkPb.newBuilder()
                        .setParttion(0)
                        .setLastKey(bookmarkKey)
                        .build();

        return Pair.of(results, bookmarkOut);
    }

    private boolean isClusterScoped(GetableClassEnum type) {
        switch (type) {
            case PRINCIPAL:
            case TENANT:
                return true;
            case TASK_DEF:
            case WF_RUN:
            case WF_SPEC:
            case NODE_RUN:
            case EXTERNAL_EVENT:
            case EXTERNAL_EVENT_DEF:
            case USER_TASK_DEF:
            case USER_TASK_RUN:
            case TASK_DEF_METRICS:
            case WF_SPEC_METRICS:
            case VARIABLE:
            case TASK_RUN:
            case TASK_WORKER_GROUP:
            case UNRECOGNIZED:
        }
        return false;
    }

    private InternalScanResponse specificPartitionTagScan(InternalScan search) {
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
                search.getStoreName(), search.getPartitionKey(), Serdes.String().serializer());
        InternalScanResponse.Builder out = InternalScanResponse.newBuilder();
        HostInfo activeHost = meta.activeHost();

        if (activeHost.equals(thisHost)) {

            ReadOnlyTenantScopedStore store = getStore(meta.partition(), false, search.getStoreName());
            String prefix = search.getTagScan().getKeyPrefix() + "/";

            try (LHKeyValueIterator<Tag> tagScanResultIterator = store.prefixScan(prefix, Tag.class)) {
                List<ByteString> matchingObjectIds = new ArrayList<>();

                while (tagScanResultIterator.hasNext()) {
                    LHIterKeyValue<Tag> currentItem = tagScanResultIterator.next();
                    Tag matchingTag = currentItem.getValue();

                    ObjectIdModel<?, ?, ?> matchingObjectId = ObjectIdModel.fromString(
                            matchingTag.getDescribedObjectId(), AbstractGetable.getIdCls(search.getObjectType()));
                    matchingObjectIds.add(ByteString.copyFrom(matchingObjectId.toBytes()));

                    if (matchingObjectIds.size() == search.getLimit()) {
                        break;
                    }
                }
                out.addAllResults(matchingObjectIds);
            }
        } else {
            InternalScanResponse reply =
                    getInternalClient(activeHost).internalScan(search.toProto().build());
            out.addAllResults(reply.getResultsList());
        }

        return out.build();
    }

    private InternalScanResponse objectIdPrefixScan(InternalScan search) throws StatusRuntimeException {
        HostInfo correctHost = getHostForKey(search.storeName, search.partitionKey);

        if (getHostForKey(search.storeName, search.partitionKey).equals(thisHost)) {
            return objectIdPrefixScanOnThisHost(search);
        } else {
            return getInternalClient(correctHost).internalScan(search.toProto().build());
        }
    }

    private InternalScanResponse objectIdPrefixScanOnThisHost(InternalScan req) {
        int curLimit = req.limit;
        BookmarkPb reqBookmark = req.bookmark;
        if (reqBookmark == null) {
            reqBookmark = BookmarkPb.newBuilder().build();
        }
        InternalScanResponse.Builder out = InternalScanResponse.newBuilder();

        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
                req.storeName, req.partitionKey, Serdes.String().serializer());
        int partition = meta.partition();

        ReadOnlyTenantScopedStore store = getStore(partition, false, req.storeName);
        PartitionBookmarkPb partBookmark = reqBookmark.getInProgressPartitionsOrDefault(partition, null);

        String endKey = req.boundedObjectIdScan.getEndObjectId() + "~";
        String startKey;
        if (partBookmark == null) {
            startKey = StoredGetable.getRocksDBKey(req.boundedObjectIdScan.getStartObjectId(), req.getObjectType());
        } else {
            startKey = partBookmark.getLastKey();
        }
        String bookmarkKey = null;
        boolean brokenBecauseOutOfData = true;

        try (LHKeyValueIterator<?> iter =
                store.range(startKey, StoredGetable.getRocksDBKey(endKey, req.getObjectType()), StoredGetable.class)) {

            while (iter.hasNext()) {
                LHIterKeyValue<? extends Storeable<?>> next = iter.next();
                if (--curLimit < 0) {
                    bookmarkKey = next.getKey();
                    brokenBecauseOutOfData = false;
                    break;
                }
                out.addResults(iterKeyValueToInternalScanResult(next, req.resultType, req.objectType));
            }
        }

        if (!brokenBecauseOutOfData) {
            // Then we have more data for the next request, so we want to return
            // a bookmark.

            PartitionBookmarkPb nextBookmark = PartitionBookmarkPb.newBuilder()
                    .setParttion(partition)
                    .setLastKey(bookmarkKey)
                    .build();

            out.setUpdatedBookmark(BookmarkPb.newBuilder().putInProgressPartitions(partition, nextBookmark));
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
            LHIterKeyValue<? extends Storeable<?>> next, ScanResultTypePb resultType, GetableClassEnum objectType) {

        if (resultType == ScanResultTypePb.OBJECT) {
            StoredGetable<?, ?> storedGetable = (StoredGetable<?, ?>) next.getValue();

            return ByteString.copyFrom(storedGetable.getStoredObject().toBytes());

        } else if (resultType == ScanResultTypePb.OBJECT_ID) {
            Class<? extends ObjectIdModel<?, ?, ?>> idCls = AbstractGetable.getIdCls(objectType);

            // TODO: This is a leaky abstraction.
            String storeableKey = next.getKey();
            Matcher matcher = objectIdExtractorPattern.matcher(storeableKey);
            if (matcher.find()) {
                int prefixEndIndex = matcher.end(0);
                String objectIdStr = storeableKey.substring(prefixEndIndex);
                return ByteString.copyFrom(
                        ObjectIdModel.fromString(objectIdStr, idCls).toBytes());
            } else {
                throw new IllegalStateException("Invalid object id");
            }
        } else {
            throw new RuntimeException("Impossible: unknown result type");
        }
    }

    private InternalScanResponse allPartitionTagScan(InternalScan search) {
        int limit = search.limit;

        // First, see what results we have locally. Then if we need more results
        // to hit the limit, we query another host.
        // How do we know which host to query? Well, we find a partition which
        // hasn't been completed yet (by consulting the Bookmark), and then
        // query the owner of that partition.

        InternalScanResponse out = localAllPartitionTagScan(search);
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
            HostInfo otherHost = getHostForPartition(getRandomUnfinishedPartition(out.getUpdatedBookmark()));
            if (otherHost.equals(thisHost)) {
                throw new IllegalStateException(
                        "Exhausted local partitions but getRandomUnfinishedPartition returned local partition");
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
            newReq.filters = search.filters;

            InternalScanResponse reply;
            reply = stub.internalScan(newReq.toProto().build());
            InternalScanResponse.Builder newOutBuilder = InternalScanResponse.newBuilder()
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
            throw new LHMisconfigurationException("Unrecognized partition");
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
        throw new LHApiException(
                Status.UNAVAILABLE, "Streams application is in %s state".formatted(coreStreams.state()));
    }

    private HostInfo getHostForKey(String storeName, String partitionKey) {
        if (storeName.equals(ServerTopology.GLOBAL_METADATA_STORE)) {
            // Every processor has the global store, so we can always do the scan locally.
            return thisHost;
        }
        KeyQueryMetadata meta = coreStreams.queryMetadataForKey(
                storeName, partitionKey, Serdes.String().serializer());
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

    private InternalScanResponse localAllPartitionTagScan(InternalScan req) {
        log.trace("Local Tag prefix scan");
        if (req.partitionKey != null) {
            throw new IllegalArgumentException("called localAllPartitionTagScan with partitionKey");
        }

        int curLimit = req.limit;

        BookmarkPb reqBookmark = req.bookmark;
        if (reqBookmark == null) {
            reqBookmark = BookmarkPb.newBuilder().build();
        }

        BookmarkPb.Builder outBookmark = reqBookmark.toBuilder();
        InternalScanResponse.Builder out = InternalScanResponse.newBuilder();

        // iterate through all active and standby local partitions
        for (int partition : getLocalActiveCommandProcessorPartitions()) {
            if (reqBookmark.getCompletedPartitionsList().contains(partition)) {
                // This partition has already been accounted for
                continue;
            }
            PartitionBookmarkPb partBookmark = null;
            if (reqBookmark != null) {
                partBookmark = reqBookmark.getInProgressPartitionsOrDefault(partition, null);
            }

            // Add all matching objects from that partition
            Pair<List<ByteString>, PartitionBookmarkPb> result = onePartitionPaginatedTagScan(
                    req.tagScan, partBookmark, curLimit, req.objectType, partition, req.filters, req.getStoreName());

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

        if (outBookmark.getCompletedPartitionsCount() < config.getClusterPartitions()) {
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
            List<ScanFilterModel> filters,
            String storeName) {
        PartitionBookmarkPb bookmarkOut = null;
        List<ByteString> idsOut = new ArrayList<>();

        String startKey;

        if (bookmark != null) {
            startKey = bookmark.getLastKey();
        } else {
            startKey = tagPrefixScan.getKeyPrefix() + "/";
            if (tagPrefixScan.hasEarliestCreateTime()) {
                startKey += LHUtil.toLhDbFormat(LHUtil.fromProtoTs(tagPrefixScan.getEarliestCreateTime()));
            }
        }
        String endKey = tagPrefixScan.getKeyPrefix() + "/";
        if (tagPrefixScan.hasLatestCreateTime()) {
            endKey += LHUtil.toLhDbFormat(LHUtil.fromProtoTs(tagPrefixScan.getLatestCreateTime()));
        }
        endKey += "~";

        BiPredicate<Tag, List<ScanFilterModel>> passesFilter = (tag, scanFilterModels) -> {
            if (tag.objectType != GetableClassEnum.WF_RUN && !filters.isEmpty()) {
                throw new LHApiException(Status.INTERNAL, "Not possible to have filters on non-wfrun scan");
            }

            WfRunIdModel wfRunId =
                    (WfRunIdModel) ObjectIdModel.fromString(tag.getDescribedObjectId(), WfRunIdModel.class);
            return filters.stream().allMatch(filter -> filter.matches(wfRunId, executionContext()));
        };

        try (LHKeyValueIterator<Tag> iter = createTagIterator(startKey, endKey, objectType, storeName, partition)) {
            boolean brokenBecauseOutOfData = true;
            while (iter.hasNext()) {
                LHIterKeyValue<Tag> next = iter.next();
                Tag tag = next.getValue();
                if (!passesFilter.test(tag, filters)) {
                    continue;
                }
                if (--limit < 0) {
                    bookmarkOut = PartitionBookmarkPb.newBuilder()
                            .setParttion(partition)
                            .setLastKey(tag.getStoreKey())
                            .build();

                    // broke loop because we filled up the limit
                    brokenBecauseOutOfData = false;
                    break;
                }

                // Turn the ID String into the ObjectId structure, then serialize it
                // to proto
                Class<? extends ObjectIdModel<?, ?, ?>> idCls = AbstractGetable.getIdCls(objectType);
                idsOut.add(ObjectIdModel.fromString(next.getValue().describedObjectId, idCls)
                        .toProto()
                        .build()
                        .toByteString());
            }

            if (brokenBecauseOutOfData) {
                bookmarkOut = null;
            }
        }
        return Pair.of(idsOut, bookmarkOut);
    }

    private LHKeyValueIterator<Tag> createTagIterator(
            String startKey, String endKey, GetableClassEnum objectType, String storeName, int specificPartition) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(specificPartition, false, storeName);
        if (isClusterScoped(objectType)) {
            ReadOnlyClusterScopedStore clusterStore =
                    ReadOnlyClusterScopedStore.newInstance(rawStore, executionContext());
            return clusterStore.range(startKey, endKey, Tag.class);
        } else {
            TenantIdModel currentTenantId = executionContext().authorization().tenantId();
            ReadOnlyTenantScopedStore tenantStore =
                    ReadOnlyTenantScopedStore.newInstance(rawStore, currentTenantId, executionContext());
            return tenantStore.range(startKey, endKey, Tag.class);
        }
    }

    private LHKeyValueIterator<?> createObjectIdIteratorGlobalStore(
            String startKey, String endKey, GetableClassEnum objectType, String storeName, int specificPartition) {
        ReadOnlyKeyValueStore<String, Bytes> rawStore = getRawStore(specificPartition, false, storeName);
        if (isClusterScoped(objectType)) {
            ReadOnlyClusterScopedStore clusterStore =
                    ReadOnlyClusterScopedStore.newInstance(rawStore, executionContext());
            return clusterStore.range(startKey, endKey, StoredGetable.class);
        } else {
            TenantIdModel currentTenantId = executionContext().authorization().tenantId();
            ReadOnlyTenantScopedStore tenantStore =
                    ReadOnlyTenantScopedStore.newInstance(rawStore, currentTenantId, executionContext());
            return tenantStore.range(startKey, endKey, StoredGetable.class);
        }
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

    private static boolean isCommandProcessor(TaskMetadata task, LHServerConfig config) {
        for (TopicPartition tPart : task.topicPartitions()) {
            if (isCommandProcessor(tPart, config)) return true;
        }
        return false;
    }

    private static boolean isCommandProcessor(TopicPartition tPart, LHServerConfig config) {
        return tPart.topic().equals(config.getCoreCmdTopicName());
    }
}
