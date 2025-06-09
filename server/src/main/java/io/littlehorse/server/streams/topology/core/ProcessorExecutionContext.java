package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.CorrelatedEventModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.global.externaleventdef.CorrelatedEventConfigModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.outputtopic.OutputTopicRecordModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.auth.internalport.InternalCallCredentials;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.EventCorrelationMarkerModel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Execution context used in the Core Sub-Topology. This is the processor where the real work of
 * scheduling WfRun's is actually done.
 */
public class ProcessorExecutionContext implements ExecutionContext {

    private final LHServerConfig config;

    private final AuthorizationContext authContext;
    private final ProcessorContext<String, CommandProcessorOutput> processorContext;
    private final MetadataCache metadataCache;
    private LHTaskManager currentTaskManager;
    private CorrelationMarkerManager correlationManager;
    private TaskQueueManager globalTaskQueueManager;
    private GetableManager storageManager;
    private final Headers recordMetadata;
    private final CommandModel currentCommand;
    private final TenantScopedStore coreStore;
    private final ReadOnlyMetadataManager metadataManager;
    private WfService service;

    private List<WorkflowEventModel> eventsToThrow;

    private final LHServer server;
    private GetableUpdates getableUpdates;
    private MetricsUpdater metricsAggregator;
    private final TenantIdModel tenantId;

    public ProcessorExecutionContext(
            Command currentCommand,
            Headers recordHeaders,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            TaskQueueManager globalTaskQueueManager,
            MetadataCache metadataCache,
            LHServer server) {

        this.processorContext = processorContext;
        this.metadataCache = metadataCache;

        ReadOnlyKeyValueStore<String, Bytes> nativeGlobalStore = nativeGlobalStore();
        this.tenantId = HeadersUtil.tenantIdFromMetadata(recordHeaders);
        ReadOnlyClusterScopedStore clusterMetadataStore =
                ReadOnlyClusterScopedStore.newInstance(nativeGlobalStore, this);
        ReadOnlyTenantScopedStore tenantMetadataStore =
                ReadOnlyTenantScopedStore.newInstance(nativeGlobalStore, tenantId, this);
        this.metadataManager = new ReadOnlyMetadataManager(clusterMetadataStore, tenantMetadataStore, metadataCache);

        this.config = config;
        this.globalTaskQueueManager = globalTaskQueueManager;
        this.recordMetadata = recordHeaders;
        this.server = server;
        this.coreStore = TenantScopedStore.newInstance(nativeCoreStore(), tenantId, this);

        this.authContext = this.authContextFor();
        this.currentCommand = LHSerializable.fromProto(currentCommand, CommandModel.class, this);
        this.eventsToThrow = new ArrayList<>();
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

    public void maybeCorrelateEventPedros(CorrelatedEventModel event) {
        EventCorrelationMarkerModel marker = getCorrelationMarkerManager()
                .getMarker(event.getId().getKey(), event.getId().getExternalEventDefId());
        if (marker != null) {
            correlate(event, marker);
        }
    }

    public void maybeCorrelateEventPedros(EventCorrelationMarkerModel marker) {
        CorrelatedEventModel candidate =
                getableManager().get(new CorrelatedEventIdModel(marker.getCorrelationKey(), marker.getEventDefId()));
        if (candidate != null) {
            correlate(candidate, marker);
        }
    }

    private void correlate(CorrelatedEventModel correlatedEvent, EventCorrelationMarkerModel marker) {
        // What we need to do is fetch the `EventCorrelationMarkerModel` if it exists, and if so:
        // 1. Delete it (if specified to do so in the `ExternalEventDefModel`)
        // 2. Forward ExternalEvents

        for (NodeRunIdModel waitingNodeRun : marker.getSourceNodeRuns()) {
            ExternalEventIdModel externalEventId = new ExternalEventIdModel(
                    waitingNodeRun.getWfRunId(),
                    correlatedEvent.getId().getExternalEventDefId(),
                    LHUtil.generateGuid());
            PutExternalEventRequestModel request = new PutExternalEventRequestModel();
            request.setWfRunId(waitingNodeRun.getWfRunId());
            request.setGuid(externalEventId.getGuid());
            request.setThreadRunNumber(waitingNodeRun.getThreadRunNumber());
            request.setNodeRunPosition(waitingNodeRun.getPosition());
            request.setContent(correlatedEvent.getContent());
            request.setExternalEventDefId(correlatedEvent.getId().getExternalEventDefId());

            correlatedEvent.getExternalEvents().add(externalEventId);

            CommandModel command = new CommandModel(request);
            command.time = new Date();

            LHTimer timer = new LHTimer(command);
            timer.key = command.getPartitionKey();
            timer.setMaturationTime(command.time);

            getTaskManager().forwardTimer(timer);
        }

        // Either save the marker or delete it
        ExternalEventDefModel externalEventDef =
                metadataManager().get(correlatedEvent.getId().getExternalEventDefId());
        CorrelatedEventConfigModel config = externalEventDef.getCorrelatedEventConfig();
        if (config.isDeleteAfterFirstCorrelation()) {
            getableManager().delete(correlatedEvent.getId());
        } else {
            getableManager().put(correlatedEvent);
        }

        getCorrelationMarkerManager().clearMarker(marker);
    }

    public CorrelationMarkerManager getCorrelationMarkerManager() {
        // I'm not so lazy that I forget to do lazy loading
        if (this.correlationManager == null) {
            this.correlationManager = new CorrelationMarkerManager(coreStore);
        }
        return this.correlationManager;
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
        storageManager = new GetableManager(
                coreStore,
                processorContext,
                config,
                currentCommand,
                this,
                metadataManager.get(tenantId).getOutputTopicConfig());
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
        if (storageManager != null) {
            storageManager.commit();
        }
        if (hasTaskManager()) {
            currentTaskManager.forwardPendingTimers();
            currentTaskManager.forwardPendingTasks();
        }
        if (metricsAggregator != null) {
            metricsAggregator.maybePersistState();
        }
        for (WorkflowEventModel event : eventsToThrow) {
            server.onEventThrown(event, authContext.tenantId());
        }
    }

    public CommandModel currentCommand() {
        return currentCommand;
    }

    public Set<HostModel> getInternalHosts() {
        return server.getAllInternalHosts();
    }

    public LHHostInfo getAdvertisedHost(HostModel host, String listenerName) {
        return server.getAdvertisedHost(host, listenerName, InternalCallCredentials.forContext(this));
    }

    public void forward(OutputTopicRecordModel thingToSend) {
        String topic = config.getExecutionOutputTopicName(authorization().tenantId());
        CommandProcessorOutput toForward =
                new CommandProcessorOutput(topic, thingToSend, thingToSend.getPartitionKey());
        processorContext.forward(new Record<>(
                toForward.partitionKey, toForward, currentCommand.getTime().getTime()));
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return config;
    }

    public GetableUpdates getableUpdates() {
        if (getableUpdates == null) {
            getableUpdates = new GetableUpdates();
            // TODO: enable metrics here
        }
        return getableUpdates;
    }

    private AuthorizationContext authContextFor() {
        PrincipalIdModel principalId = HeadersUtil.principalIdFromMetadata(recordMetadata);
        TenantIdModel tenantId = HeadersUtil.tenantIdFromMetadata(recordMetadata);
        // TODO: get current acls for principal and isAdmin boolean. It is required for fine-grained acls verification
        return new AuthorizationContextImpl(principalId, tenantId, List.of(), false);
    }

    private KeyValueStore<String, Bytes> nativeCoreStore() {
        return processorContext.getStateStore(ServerTopology.CORE_STORE);
    }

    private ReadOnlyKeyValueStore<String, Bytes> nativeGlobalStore() {
        return processorContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
    }

    public void notifyOfEventThrown(WorkflowEventModel event) {
        this.eventsToThrow.add(event);
    }
}
