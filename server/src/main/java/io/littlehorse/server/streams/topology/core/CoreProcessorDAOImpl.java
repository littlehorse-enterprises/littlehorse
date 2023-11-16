package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.AnalyticsRegistry;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.storeinternals.GetableStorageManager;
import io.littlehorse.server.streams.util.InternalHosts;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class CoreProcessorDAOImpl extends CoreProcessorDAO {

    private CommandModel command;
    private final KafkaStreamsServerImpl server;
    private Set<HostModel> currentHosts;

    private final ProcessorContext<String, CommandProcessorOutput> ctx;
    private final LHServerConfig config;
    private boolean partitionIsClaimed;

    private GetableStorageManager storageManager;

    private final ModelStore coreStore;

    public CoreProcessorDAOImpl(
            final ProcessorContext<String, CommandProcessorOutput> ctx,
            final LHServerConfig config,
            final KafkaStreamsServerImpl server,
            final MetadataCache metadataCache,
            final ReadOnlyModelStore metadataStore,
            final ModelStore coreStore,
            final AuthorizationContext context) {
        super(metadataStore, metadataCache, context);
        this.coreStore = coreStore;
        this.server = server;
        this.ctx = ctx;
        this.config = config;

        // At the start, we haven't claimed the partition until the claim event comes
        this.partitionIsClaimed = false;
    }

    @Override
    public void initCommand(CommandModel command, KeyValueStore<String, Bytes> nativeStore, Headers metadataHeaders) {
        this.command = command;
        this.storageManager = new GetableStorageManager(this.coreStore, ctx, config, command, this);
    }

    @Override
    public CommandModel getCommand() {
        return command;
    }

    @Override
    public void commit() {}

    @Override
    public <U extends Message, T extends AbstractGetable<U>> T get(ObjectIdModel<?, U, T> id) {
        return storageManager.get(id);
    }

    @Override
    public void put(CoreGetable<?> getable) {
        storageManager.put(getable);
    }

    @Override
    public void delete(WfRunIdModel id) {
        WfRunModel wfRun = get(id);
        if (wfRun == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfRun");
        }

        storageManager.delete(id);
        storageManager.deleteAllByPrefix(id.getId(), NodeRunModel.class);
        storageManager.deleteAllByPrefix(id.getId(), TaskRunModel.class);
        storageManager.deleteAllByPrefix(id.getId(), ExternalEventModel.class);
        storageManager.deleteAllByPrefix(id.getId(), VariableModel.class);
        storageManager.deleteAllByPrefix(id.getId(), UserTaskRunModel.class);
    }

    @Override
    public WfSpecModel getWfSpec(String name, Integer version) {
        return super.getWfSpec(name, version);
    }

    @Override
    public void delete(ExternalEventIdModel id) {
        ExternalEventModel eev = get(id);
        if (eev == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified ExternalEvent");
        }
        if (eev.isClaimed()) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "Specified ExternalEvent already claimed by WfRun");
        }
        storageManager.delete(id);
    }

    @Override
    public ExternalEventModel getUnclaimedEvent(String wfRunId, String externalEventDefName) {

        String extEvtPrefix = ExternalEventModel.getStorePrefix(wfRunId, externalEventDefName);

        return storageManager.getFirstByCreatedTimeFromPrefix(
                extEvtPrefix, ExternalEventModel.class, externalEvent -> !externalEvent.isClaimed());
    }

    @Override
    public String getCoreCmdTopic() {
        return config.getCoreCmdTopicName();
    }

    @Override
    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;

        try (LHKeyValueIterator<ScheduledTaskModel> iter = this.coreStore.prefixScan("", ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.debug("Rehydration: scheduling task: {}", scheduledTask.getStoreKey());
                server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
            }
        }
    }

    /*private void forwardTask(ScheduledTaskModel scheduledTask) {
        this.coreStore.put(scheduledTask);

        server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
    }*/

    /*private void forwardTimer(LHTimer timer) {
        CommandProcessorOutput output = new CommandProcessorOutput(config.getTimerTopic(), timer, timer.key);
        Headers headers =
                HeadersUtil.metadataHeadersFor(context().tenantId(), context().principalId());
        ctx.forward(new Record<>(timer.key, output, System.currentTimeMillis(), headers));
    }*/

    @Override
    public LHHostInfo getAdvertisedHost(HostModel host, String listenerName) {
        return server.getAdvertisedHost(host, listenerName);
    }

    @Override
    public InternalHosts getInternalHosts() {
        Set<HostModel> newHost = server.getAllInternalHosts();
        InternalHosts internalHosts = new InternalHosts(currentHosts, newHost);
        currentHosts = newHost;
        return internalHosts;
    }

    @Override
    public AnalyticsRegistry getRegistry() {
        throw new NotImplementedException("TODO: Re-enable metrics/analytics");
    }
}
