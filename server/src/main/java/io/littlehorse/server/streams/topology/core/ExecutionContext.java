package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.GetableStorageManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.InternalHosts;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import java.util.Set;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * This class is intended to be used as a context propagator from
 * Processors to Subcommands. Provides a set of method to build dependencies
 * for the sub commands
 * Each Record will have its instance for this class
 */
public final class ExecutionContext {

    private final LHServerConfig config;

    private final AuthorizationContext authContext;

    private final ProcessorContext<String, CommandProcessorOutput> timerContext;

    private final KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache;

    private final boolean isClusterLevelCommand;
    private LHTaskManager currentTaskManager;
    private TaskQueueManager globalTaskQueueManager;
    private GetableStorageManager storageManager;

    private Set<HostModel> currentHosts;

    public ExecutionContext(
            LHServerConfig config,
            AuthorizationContext authContext,
            ProcessorContext<String, CommandProcessorOutput> timerContext,
            KafkaStreamsServerImpl server,
            boolean isClusterLevelCommand,
            TaskQueueManager globalTaskQueueManager) {
        this.config = config;
        this.authContext = authContext;
        this.timerContext = timerContext;
        this.server = server;
        this.metadataCache = new MetadataCache();
        this.isClusterLevelCommand = isClusterLevelCommand;
        this.globalTaskQueueManager = globalTaskQueueManager;
    }

    /**
     * Gets an instance for {@link LHTaskManager} class
     */
    public LHTaskManager getTaskManager() {
        if (currentTaskManager != null) {
            return currentTaskManager;
        }
        currentTaskManager = new LHTaskManager(
                config.getTimerTopic(),
                authContext,
                timerContext,
                globalTaskQueueManager,
                storeFor(authContext.tenantId(), coreStore()));
        return currentTaskManager;
    }

    public AuthorizationContext authorization() {
        return authContext;
    }

    public GetableStorageManager getStorageManager() {
        return null;
    }

    public WfService wfService() {
        return null;
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
        return null;
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

    private ModelStore storeFor(String tenantId, KeyValueStore<String, Bytes> nativeStore) {
        ModelStore store;
        if (isClusterLevelCommand) {
            store = ModelStore.defaultStore(nativeStore, this);
        } else {
            store = ModelStore.instanceFor(nativeStore, tenantId, this);
        }
        return store;
    }

    private KeyValueStore<String, Bytes> globalMetadata() {
        return timerContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
    }

    private KeyValueStore<String, Bytes> coreStore() {
        return timerContext.getStateStore(ServerTopology.CORE_STORE);
    }

    private AuthorizationContext contextFor(String principalId, String tenantId) {
        // TODO: for fine-grained acl verification we will need to find list of ACLS for the principalId and
        // tenantId
        List<ServerACLModel> currentAcls = List.of();
        return new AuthorizationContextImpl(principalId, tenantId, currentAcls);
    }

    private AuthorizationContext defaultContext(String principalId) {
        return new AuthorizationContextImpl(principalId, ModelStore.DEFAULT_TENANT, List.of());
    }

}
