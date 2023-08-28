package io.littlehorse.server.streams.topology.core;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.AnalyticsRegistry;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
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
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ReadOnlyRocksDBWrapper;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.storeinternals.GetableStorageManager;
import io.littlehorse.server.streams.util.InternalHosts;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

@Slf4j
public class CoreProcessorDAOImpl extends CoreProcessorDAO {

    private Map<String, ScheduledTaskModel> scheduledTaskPuts;
    private List<LHTimer> timersToSchedule;
    private CommandModel command;
    private KafkaStreamsServerImpl server;
    private Set<HostModel> currentHosts;

    private RocksDBWrapper rocksdb;
    private ReadOnlyMetadataStore globalStore;
    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private LHServerConfig config;
    private boolean partitionIsClaimed;

    private GetableStorageManager storageManager;

    public CoreProcessorDAOImpl(
            final ProcessorContext<String, CommandProcessorOutput> ctx,
            LHServerConfig config,
            KafkaStreamsServerImpl server,
            MetadataCache wfSpecCache,
            RocksDBWrapper localStore,
            ReadOnlyRocksDBWrapper globalStore) {
        super(globalStore);

        this.server = server;
        this.ctx = ctx;
        this.config = config;
        this.rocksdb = localStore;
        this.globalStore = new ReadOnlyMetadataStore(globalStore);

        // At the start, we haven't claimed the partition until the claim event comes
        this.partitionIsClaimed = false;

        scheduledTaskPuts = new HashMap<>();
        timersToSchedule = new ArrayList<>();
    }

    @Override
    public void initCommand(CommandModel command) {
        scheduledTaskPuts.clear();
        timersToSchedule.clear();
        this.command = command;
        this.storageManager = new GetableStorageManager(rocksdb, ctx, config, command, this);
    }

    @Override
    public CommandModel getCommand() {
        return command;
    }

    @Override
    public void commit() {
        storageManager.commit();
        for (LHTimer timer : timersToSchedule) {
            forwardTimer(timer);
        }

        for (Map.Entry<String, ScheduledTaskModel> entry : scheduledTaskPuts.entrySet()) {
            String scheduledTaskId = entry.getKey();
            ScheduledTaskModel scheduledTask = entry.getValue();
            if (scheduledTask != null) {
                forwardTask(scheduledTask);
            } else {
                rocksdb.delete(scheduledTaskId, StoreableType.SCHEDULED_TASK);
            }
        }
        clearThingsToWrite();
    }

    @Override
    public <U extends Message, T extends CoreGetable<U>> T get(ObjectIdModel<?, U, T> id) {
        System.out.println(id);
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
        WfSpecModel wfSpec = globalStore.getWfSpec(name, version);
        if (wfSpec != null) {
            wfSpec.setDao(this);
        }
        return wfSpec;
    }

    @Override
    public TaskDefModel getTaskDef(String name) {
        return globalStore.getTaskDef(name);
    }

    @Override
    public ExternalEventDefModel getExternalEventDef(String name) {
        return globalStore.getExternalEventDef(name);
    }

    @Override
    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        return globalStore.getUserTaskDef(name, version);
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
    public void scheduleTask(ScheduledTaskModel scheduledTask) {
        scheduledTaskPuts.put(scheduledTask.getStoreKey(), scheduledTask);
    }

    @Override
    public void scheduleTimer(LHTimer timer) {
        timersToSchedule.add(timer);
    }

    @Override
    public ScheduledTaskModel markTaskAsScheduled(TaskRunIdModel taskRunId) {
        ScheduledTaskModel scheduledTask = rocksdb.get(taskRunId.toString(), ScheduledTaskModel.class);

        if (scheduledTask != null) {
            scheduledTaskPuts.put(scheduledTask.getStoreKey(), null);
        }

        return scheduledTask;
    }

    // // This method should only be called if we have a serious unknown bug in
    // // LittleHorse that causes an unexpected exception to occur while executing
    // // CommandProcessor#process().
    // @Override
    // public void abortChangesAndMarkWfRunFailed(Throwable failure, String wfRunId)
    // {
    // // if the wfRun exists: we want to mark it as failed with a message.
    // // Else, do nothing.
    // WfRunModel wfRunModel = storageManager.get(wfRunId, WfRunModel.class);
    // if (wfRunModel != null) {
    // log.warn("Marking wfRun {} as failed due to internal LH exception", wfRunId);
    // ThreadRunModel entrypoint = wfRunModel.getThreadRun(0);
    // entrypoint.setStatus(LHStatus.ERROR);

    // String message = "Had an internal LH failur processing command of type "
    // + command.getType()
    // + ": "
    // + failure.getMessage();
    // entrypoint.setErrorMessage(message);
    // storageManager.abortAndUpdate(wfRunModel);
    // } else {
    // log.warn("Caught internal LH error but found no WfRun with id {}", wfRunId);
    // }
    // clearThingsToWrite();
    // }

    @Override
    public String getCoreCmdTopic() {
        return config.getCoreCmdTopicName();
    }

    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;

        try (LHKeyValueIterator<ScheduledTaskModel> iter = rocksdb.prefixScan("", ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.debug("Rehydration: scheduling task: {}", scheduledTask.getStoreKey());
                server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
            }
        }
    }

    private void forwardTask(ScheduledTaskModel scheduledTask) {
        rocksdb.put(scheduledTask);

        if (partitionIsClaimed) {
            server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
        } else {
            // We will call onTaskScheduled() when we re-hydrate it.
            log.debug("Haven't claimed partitions, deferring scheduling of tsr");
        }
    }

    private void forwardTimer(LHTimer timer) {
        CommandProcessorOutput output = new CommandProcessorOutput(config.getTimerTopic(), timer, timer.key);

        ctx.forward(new Record<String, CommandProcessorOutput>(timer.key, output, System.currentTimeMillis()));
    }

    private void clearThingsToWrite() {
        scheduledTaskPuts.clear();
        timersToSchedule.clear();
    }

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
