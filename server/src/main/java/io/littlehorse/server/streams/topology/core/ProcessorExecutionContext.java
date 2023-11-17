package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.ClusterLevelCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.InternalHosts;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;
import java.util.Set;

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

    private final KafkaStreamsServerImpl server;

    public ProcessorExecutionContext(
            CommandModel currentCommand,
            Headers recordMetadata,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            TaskQueueManager globalTaskQueueManager,
            MetadataCache metadataCache,
            KafkaStreamsServerImpl server) {
        this.config = config;
        this.processorContext = processorContext;
        this.metadataCache = metadataCache;
        this.isClusterLevelCommand = currentCommand instanceof ClusterLevelCommand;
        this.globalTaskQueueManager = globalTaskQueueManager;
        this.recordMetadata = recordMetadata;
        this.currentCommand = currentCommand;
        this.server = server;
        this.coreStore = storeFor(HeadersUtil.tenantIdFromMetadata(recordMetadata), nativeCoreStore());
        this.authContext = this.authContextFor();
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
                authContext,
                processorContext,
                globalTaskQueueManager,
                coreStore);
        return currentTaskManager;
    }

    public AuthorizationContext authorization() {
        return authContext;
    }

    // Lazy loading for a getable manager
    public GetableManager getableManager() {
        if(storageManager != null){
            return storageManager;
        }
        storageManager = new GetableManager(coreStore, processorContext, config, currentCommand, this);
        return storageManager;
    }

    @Override
    public WfService service() {
        return new WfService(this.coreStore, );
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

    @Override
    public ReadOnlyModelStore store() {
        return coreStore;
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
    private AuthorizationContext authContextFor(){
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

}
