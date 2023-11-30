package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.InternalHosts;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import java.util.Set;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class ProcessorExecutionContext implements ExecutionContext {

    private final LHServerConfig config;

    private final AuthorizationContext authContext;
    private final ProcessorContext<String, CommandProcessorOutput> processorContext;
    private final MetadataCache metadataCache;
    private final boolean isClusterLevelCommand;
    private LHTaskManager currentTaskManager;
    private TaskQueueManager globalTaskQueueManager;
    private GetableManager storageManager;

    private Set<HostModel> currentHosts;
    private final Headers recordMetadata;
    private final CommandModel currentCommand;
    private final ModelStore coreStore;
    private final ReadOnlyMetadataManager metadataManager;
    private WfService service;

    private final KafkaStreamsServerImpl server;

    public ProcessorExecutionContext(
            Command currentCommand,
            Headers recordMetadata,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            TaskQueueManager globalTaskQueueManager,
            MetadataCache metadataCache,
            KafkaStreamsServerImpl server) {
        this.processorContext = processorContext;
        KeyValueStore<String, Bytes> nativeGlobalStore = nativeGlobalStore();
        this.config = config;
        this.metadataCache = metadataCache;
        this.globalTaskQueueManager = globalTaskQueueManager;
        this.recordMetadata = recordMetadata;
        this.server = server;
        this.metadataManager = new ReadOnlyMetadataManager(
                ModelStore.defaultStore(nativeGlobalStore, this),
                ModelStore.tenantStoreFor(nativeGlobalStore, HeadersUtil.tenantIdFromMetadata(recordMetadata), this));
        this.authContext = this.authContextFor();
        this.currentCommand = LHSerializable.fromProto(currentCommand, CommandModel.class, this);
        this.isClusterLevelCommand = this.currentCommand instanceof ClusterLevelCommand;
        this.coreStore = storeFor(HeadersUtil.tenantIdFromMetadata(recordMetadata), nativeCoreStore());
    }

    /**
     * Lazy loading for a task manager
     * Gets an instance for {@link LHTaskManager} class
     */
    public LHTaskManager getTaskManager() {
        if (currentTaskManager != null) {
            return currentTaskManager;
        }
        currentTaskManager = new LHTaskManager(
                config.getTimerTopic(),
                config.getCoreCmdTopicName(),
                authContext,
                processorContext,
                globalTaskQueueManager,
                coreStore);
        return currentTaskManager;
    }

    @Override
    public AuthorizationContext authorization() {
        return authContext;
    }

    // Lazy loading for a getable manager
    public GetableManager getableManager() {
        if (storageManager != null) {
            return storageManager;
        }
        storageManager = new GetableManager(coreStore, processorContext, config, currentCommand, this);
        return storageManager;
    }

    @Override
    public WfService service() {
        if (service != null) {
            return service;
        }
        service = new WfService(this.metadataManager, metadataCache, this);
        return service;
    }

    public boolean hasTaskManager() {
        return currentTaskManager != null;
    }

    /**
     * This will persist any pending record in the store
     * or forward any pending action. Only processors can
     * decide when to call this method
     */
    public void endExecution() {
        storageManager.commit();
        if (hasTaskManager()) {
            currentTaskManager.forwardPendingTimers();
            currentTaskManager.forwardPendingTasks();
        }
    }

    public CommandModel currentCommand() {
        return currentCommand;
    }

    public InternalHosts getInternalHosts() {
        Set<HostModel> newHost = server.getAllInternalHosts();
        InternalHosts internalHosts = new InternalHosts(currentHosts, newHost);
        currentHosts = newHost;
        return internalHosts;
    }

    public LHHostInfo getAdvertisedHost(HostModel host, String listenerName) {
        return server.getAdvertisedHost(host, listenerName);
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return config;
    }

    private AuthorizationContext authContextFor() {
        String principalId = HeadersUtil.principalIdFromMetadata(recordMetadata);
        String tenantId = HeadersUtil.tenantIdFromMetadata(recordMetadata);
        // TODO: get current acls for principal and isAdmin boolean. It is required for fine-grained acls verification
        return new AuthorizationContextImpl(principalId, tenantId, List.of(), false);
    }

    private ModelStore storeFor(String tenantId, KeyValueStore<String, Bytes> nativeStore) {
        ModelStore store;
        if (isClusterLevelCommand) {
            store = ModelStore.defaultStore(nativeStore, this);
        } else {
            store = ModelStore.instanceFor(nativeStore, tenantId, this);
        }
        return store;
    }

    private KeyValueStore<String, Bytes> nativeCoreStore() {
        return processorContext.getStateStore(ServerTopology.CORE_STORE);
    }

    private KeyValueStore<String, Bytes> nativeGlobalStore() {
        return processorContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
    }
}
