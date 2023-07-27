package io.littlehorse.server.streamsimpl.coreprocessors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.Host;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.HostInfoPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.MetricsWindowLengthPb;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.server.streamsimpl.storeinternals.GetableStorageManager;
import io.littlehorse.server.streamsimpl.storeinternals.LHROStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import io.littlehorse.server.streamsimpl.util.InternalHosts;
import io.littlehorse.server.streamsimpl.util.WfSpecCache;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class KafkaStreamsLHDAOImpl implements LHDAO {

    private Map<String, NodeRun> nodeRunPuts;
    private Map<String, Variable> variablePuts;
    private Map<String, ExternalEvent> extEvtPuts;
    private Map<String, WfRun> wfRunPuts;
    private Map<String, WfSpec> wfSpecPuts;
    private Map<String, TaskDef> taskDefPuts;
    private Map<String, TaskRun> taskRunPuts;
    private Map<String, UserTaskRun> userTaskRunPuts;
    private Map<String, UserTaskDef> userTaskDefPuts;
    private Map<String, ExternalEventDef> extEvtDefPuts;
    private Map<String, ScheduledTask> scheduledTaskPuts;
    private Map<String, TaskWorkerGroup> taskWorkerGroupPuts;
    private List<LHTimer> timersToSchedule;
    private Command command;
    private KafkaStreamsServerImpl server;
    private Map<String, TaskMetricUpdate> taskMetricPuts;
    private Map<String, WfMetricUpdate> wfMetricPuts;
    private Set<Host> currentHosts;

    /*
     * Certain metadata objects (eg. WfSpec, TaskDef, ExternalEventDef) are "global"
     * in nature. This means three things:
     * 1) Changes to them are low-throughput and infrequent
     * 2) There are a relatively small number of them
     * 3) Other resources (eg. WfRun) need to constantly consult them in order to run.
     *
     * Items 1) and 2) imply that these metadata objects can be stored on one node.
     * Item 3) implies that we *need* every node to have a local copy of these
     * objects.
     *
     * Furthermore, WfSpec's and TaskDef's depend on each other, and we want
     * strongly consistent processing (so that we can't accidentally delete a TaskDef
     * while processing a WfSpec that uses it, for example). Therefore, we want
     * all of the processing to be linearized, and therefore it needs to occur on
     * the same partition.
     *
     * So what do we do? We use the same getParttionKey() for all of the global
     * metadata.
     *
     * The boolean below here is true when this processor owns the "hot" partition.
     */
    private boolean isHotMetadataPartition;

    private LHStoreWrapper localStore;
    private LHROStoreWrapper globalStore;
    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private LHConfig config;
    private final WfSpecCache wfSpecCache;
    private boolean partitionIsClaimed;

    private GetableStorageManager getableStorageManager;

    public KafkaStreamsLHDAOImpl(
        final ProcessorContext<String, CommandProcessorOutput> ctx,
        LHConfig config,
        KafkaStreamsServerImpl server,
        WfSpecCache wfSpecCache
    ) {
        this.server = server;
        this.ctx = ctx;
        this.config = config;
        this.wfSpecCache = wfSpecCache;

        // At the start, we haven't claimed the partition until the claim event comes
        this.partitionIsClaimed = false;

        nodeRunPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        extEvtPuts = new HashMap<>();
        wfRunPuts = new HashMap<>();
        wfSpecPuts = new HashMap<>();
        extEvtDefPuts = new HashMap<>();
        taskDefPuts = new HashMap<>();
        userTaskDefPuts = new HashMap<>();
        taskMetricPuts = new HashMap<>();
        wfMetricPuts = new HashMap<>();
        taskWorkerGroupPuts = new HashMap<>();
        taskRunPuts = new HashMap<>();
        userTaskRunPuts = new HashMap<>();

        // TODO: Here is where we want to eventually add some cacheing for GET to
        // the WfSpec and TaskDef etc.

        isHotMetadataPartition =
            ctx.taskId().partition() == config.getHotMetadataPartition();

        KeyValueStore<String, Bytes> rawLocalStore = ctx.getStateStore(
            ServerTopology.CORE_STORE
        );
        ReadOnlyKeyValueStore<String, Bytes> rawGlobalStore = ctx.getStateStore(
            ServerTopology.GLOBAL_STORE
        );
        localStore = new LHStoreWrapper(rawLocalStore, config);
        globalStore = new LHROStoreWrapper(rawGlobalStore, config);

        getableStorageManager = new GetableStorageManager(localStore, config, ctx);

        scheduledTaskPuts = new HashMap<>();
        timersToSchedule = new ArrayList<>();
    }

    @Override
    public void putNodeRun(NodeRun nr) {
        nodeRunPuts.put(nr.getStoreKey(), nr);
    }

    @Override
    public NodeRun getNodeRun(String wfRunId, int threadNum, int position) {
        String key = new NodeRunId(wfRunId, threadNum, position).getStoreKey();
        if (nodeRunPuts.containsKey(key)) {
            return nodeRunPuts.get(key);
        }
        NodeRun out = localStore.get(key, NodeRun.class);

        // Little trick so that if it gets modified it is automatically saved
        if (out != null) {
            nodeRunPuts.put(key, out);
            out.setDao(this);
        }
        return out;
    }

    @Override
    public void putTaskRun(TaskRun tr) {
        taskRunPuts.put(tr.getStoreKey(), tr);
    }

    @Override
    public TaskRun getTaskRun(TaskRunId taskRunId) {
        String key = taskRunId.getStoreKey();
        if (taskRunPuts.containsKey(key)) {
            return taskRunPuts.get(key);
        }
        TaskRun out = localStore.get(key, TaskRun.class);
        if (out != null) {
            taskRunPuts.put(key, out);
            out.setDao(this);
        }
        return out;
    }

    @Override
    public void putUserTaskRun(UserTaskRun utr) {
        userTaskRunPuts.put(utr.getStoreKey(), utr);
    }

    @Override
    public UserTaskRun getUserTaskRun(UserTaskRunId userTaskRunId) {
        String key = userTaskRunId.getStoreKey();
        if (userTaskRunPuts.containsKey(key)) {
            return userTaskRunPuts.get(key);
        }
        UserTaskRun out = localStore.get(key, UserTaskRun.class);
        if (out != null) {
            userTaskRunPuts.put(key, out);
            out.setDao(this);
        }
        return out;
    }

    @Override
    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public Command getCommand() {
        return this.command;
    }

    @Override
    public void putWfSpec(WfSpec spec) {
        if (!isHotMetadataPartition) {
            throw new RuntimeException(
                "Tried to put metadata despite being on the wrong partition!"
            );
        }
        wfSpecPuts.put(spec.getStoreKey(), spec);
    }

    @Override
    public void putExternalEventDef(ExternalEventDef spec) {
        if (!isHotMetadataPartition) {
            throw new RuntimeException(
                "Tried to put metadata despite being on the wrong partition!"
            );
        }
        extEvtDefPuts.put(spec.getStoreKey(), spec);
    }

    @Override
    public void putTaskDef(TaskDef spec) {
        if (!isHotMetadataPartition) {
            throw new RuntimeException(
                "Tried to put metadata despite being on the wrong partition!"
            );
        }
        taskDefPuts.put(spec.getStoreKey(), spec);
    }

    // TODO: Investigate whether there is a potential issue with
    // Read-Your-Own-Writes if a process() method does:
    //
    //   dao.putWfSpec(foo)
    //   dao.getWfSpec("foo", null)
    //
    // It would return the last wfSpec version before the one called by put().
    // However, that doesn't happen in the code now; we should file a JIRA to
    // take care of it for later.
    @Override
    public WfSpec getWfSpec(String name, Integer version) {
        Supplier<WfSpec> findWfSpec = () -> {
            LHROStoreWrapper store = isHotMetadataPartition
                ? localStore
                : globalStore;
            if (version != null) {
                return store.get(
                    new WfSpecId(name, version).getStoreKey(),
                    WfSpec.class
                );
            }
            return store.getLastFromPrefix(name, WfSpec.class);
        };
        WfSpec wfSpec = wfSpecCache.getOrCache(name, version, findWfSpec);
        if (wfSpec != null) wfSpec.setDao(this);
        return wfSpec;
    }

    // TODO: Investigate whether there is a potential issue with
    // Read-Your-Own-Writes if a process() method does:
    //
    //   dao.putUserTaskDef(foo)
    //   dao.getUsertaskDef("foo", null)
    //
    // It would return the last UserTaskDef version before the one called by put().
    // However, that doesn't happen in the code now; we should file a JIRA to
    // take care of it for later.
    @Override
    public UserTaskDef getUserTaskDef(String name, Integer version) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        UserTaskDef out;
        if (version != null) {
            // First check the most recent puts
            out =
                store.get(
                    new UserTaskDefId(name, version).getStoreKey(),
                    UserTaskDef.class
                );
        } else {
            out = store.getLastFromPrefix(name, UserTaskDef.class);
        }
        if (out != null) out.setDao(this);
        return out;
    }

    // Same R-Y-O-W Issue
    @Override
    public void putUserTaskDef(UserTaskDef spec) {
        if (!isHotMetadataPartition) {
            throw new RuntimeException(
                "Tried to put metadata despite being on the wrong partition!"
            );
        }
        userTaskDefPuts.put(spec.getStoreKey(), spec);
    }

    // Same R-Y-O-W issue
    @Override
    public TaskDef getTaskDef(String name) {
        TaskDef out = taskDefPuts.get(name);
        if (out != null) return out;

        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        return store.get(new TaskDefId(name).getStoreKey(), TaskDef.class);
    }

    // Same here, same R-Y-O-W issue
    @Override
    public ExternalEventDef getExternalEventDef(String name) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        ExternalEventDef out = store.get(
            new ExternalEventDefId(name).getStoreKey(),
            ExternalEventDef.class
        );
        if (out != null) {
            out.setDao(this);
        }
        return out;
    }

    @Override
    public LHGlobalMetaStores getGlobalMetaStores() {
        return this;
    }

    @Override
    public void putVariable(Variable var) {
        variablePuts.put(var.getStoreKey(), var);
    }

    @Override
    public Variable getVariable(String wfRunId, String name, int threadNum) {
        String key = new VariableId(wfRunId, threadNum, name).getStoreKey();
        if (variablePuts.containsKey(key)) {
            return variablePuts.get(key);
        }
        Variable out = localStore.get(key, Variable.class);
        if (out != null) {
            variablePuts.put(key, out);
            // Need to get the WfSpec
            WfRun wfRun = getWfRun(wfRunId);
            out.setWfSpec(wfRun.getWfSpec());
            out.setDao(this);
        }
        return out;
    }

    @Override
    public ExternalEvent getUnclaimedEvent(
        String wfRunId,
        String externalEventDefName
    ) {
        // Need to load all of them and then get the least recent that hasn't been
        // claimed yet.
        String extEvtPrefix = ExternalEvent.getStorePrefix(
            wfRunId,
            externalEventDefName
        );

        for (String extEvtId : extEvtPuts.keySet()) {
            if (extEvtId.startsWith(extEvtPrefix)) {
                return extEvtPuts.get(extEvtId);
            }
        }

        // TODO: This is O(N) for number of events correlated with the WfRun.
        // Generally that will only be a small number, but there could be weird
        // use-cases where this could take a long time (if there's 1000 events or
        // so then it could take seconds, which holds up the entire scheduling).
        ExternalEvent out = null;
        try (
            LHKeyValueIterator<ExternalEvent> iter = localStore.prefixScan(
                extEvtPrefix,
                ExternalEvent.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<ExternalEvent> kvp = iter.next();
                ExternalEvent candidate;
                if (extEvtPuts.containsKey(kvp.getKey())) {
                    candidate = extEvtPuts.get(kvp.getKey());
                } else {
                    candidate = kvp.getValue();
                    extEvtPuts.put(kvp.getKey(), candidate); // TODO: Is this necessary?
                }

                if (candidate.claimed) {
                    continue;
                }

                if (
                    out == null ||
                    out.getCreatedAt().getTime() > candidate.getCreatedAt().getTime()
                ) {
                    out = candidate;
                }
            }
        }

        return out;
    }

    @Override
    public ExternalEvent getExternalEvent(String externalEventId) {
        if (extEvtPuts.containsKey(externalEventId)) {
            return extEvtPuts.get(externalEventId);
        }
        ExternalEvent out = localStore.get(externalEventId, ExternalEvent.class);
        if (out != null) {
            extEvtPuts.put(externalEventId, out);
            out.setDao(this);
        }
        return out;
    }

    @Override
    public void saveExternalEvent(ExternalEvent evt) {
        extEvtPuts.put(evt.getStoreKey(), evt);
    }

    @Override
    public void scheduleTask(ScheduledTask scheduledTask) {
        scheduledTaskPuts.put(scheduledTask.getStoreKey(), scheduledTask);
    }

    @Override
    public void scheduleTimer(LHTimer timer) {
        timersToSchedule.add(timer);
    }

    @Override
    public ScheduledTask markTaskAsScheduled(TaskRunId taskRunId) {
        ScheduledTask scheduledTask = localStore.get(
            taskRunId.getStoreKey(),
            ScheduledTask.class
        );

        if (scheduledTask != null) {
            scheduledTaskPuts.put(scheduledTask.getStoreKey(), null);
        }

        return scheduledTask;
    }

    @Override
    public WfRun getWfRun(String id) {
        if (wfRunPuts.containsKey(id)) {
            return wfRunPuts.get(id);
        }
        WfRun out = localStore.get(id, WfRun.class);
        if (out != null) {
            wfRunPuts.put(id, out);
            out.setWfSpec(getWfSpec(out.wfSpecName, out.wfSpecVersion));
            out.setDao(this);
        }
        return out;
    }

    @Override
    public void saveWfRun(WfRun wfRun) {
        wfRunPuts.put(wfRun.getStoreKey(), wfRun);
    }

    @Override
    public void commitChanges() {
        flush();
        clearThingsToWrite();
    }

    @Override
    public void abortChanges() {
        // The contract for this is to cancel any changes. Nothing gets written
        // to rocksdb until commitChanges() successfully returns; therefore,
        // all we have to do is clear the things we were gonna write.
        clearThingsToWrite();
    }

    // This method should only be called if we have a serious unknown bug in
    // LittleHorse that causes an unexpected exception to occur while executing
    // CommandProcessor#process().
    @Override
    public void abortChangesAndMarkWfRunFailed(String message) {
        for (Map.Entry<String, WfRun> e : wfRunPuts.entrySet()) {
            WfRun affected = e.getValue();
            if (e != null) {
                Failure failure = new Failure(
                    "Unknown internal error while processing: " + message,
                    LHConstants.INTERNAL_ERROR
                );
                NodeRun currentNodeRun = affected.threadRuns
                    .get(0)
                    .getCurrentNodeRun();
                currentNodeRun.fail(failure, new Date());

                saveOrDeleteGETableFlush(e.getKey(), affected, WfRun.class);
                saveOrDeleteGETableFlush(e.getKey(), currentNodeRun, NodeRun.class);
            }
        }

        clearThingsToWrite();
    }

    @Override
    public String getCoreCmdTopic() {
        return config.getCoreCmdTopicName();
    }

    @Override
    public DeleteObjectReply deleteWfRun(String wfRunId) {
        DeleteObjectReply out = new DeleteObjectReply();
        WfRun wfRun = getWfRun(wfRunId);
        if (wfRun == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Couldn't find wfRun with provided ID.";
        } else {
            if (wfRun.isRunning()) {
                out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
                out.message = "Specified wfRun is still RUNNING!";
            } else {
                wfRunPuts.put(wfRunId, null);
                deleteAllChildren(wfRun);
                out.code = LHResponseCodePb.OK;
            }
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteTaskDef(String name) {
        TaskDef toDelete = getTaskDef(name);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            taskDefPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCodePb.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteUserTaskDef(String name, int version) {
        UserTaskDef toDelete = getUserTaskDef(name, version);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            userTaskDefPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCodePb.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteWfSpec(String name, int version) {
        WfSpec toDelete = getWfSpec(name, version);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            wfSpecPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCodePb.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteExternalEventDef(String name) {
        ExternalEventDef toDelete = getExternalEventDef(name);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            extEvtDefPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCodePb.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteExternalEvent(String externalEventId) {
        ExternalEvent toDelete = getExternalEvent(externalEventId);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            extEvtPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCodePb.OK;
        }
        return out;
    }

    /*
     * Delete the following things from the wfRun:
     * - NodeRun
     * - Variable
     * - ExternalEvent
     */
    private void deleteAllChildren(WfRun wfRun) {
        String prefix = wfRun.id;
        try (
            LHKeyValueIterator<NodeRun> iter = localStore.prefixScan(
                prefix,
                NodeRun.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<NodeRun> next = iter.next();
                getableStorageManager.delete(next.getKey(), NodeRun.class);
            }
        }

        try (
            LHKeyValueIterator<Variable> iter = localStore.prefixScan(
                prefix,
                Variable.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<Variable> next = iter.next();
                getableStorageManager.delete(next.getKey(), Variable.class);
            }
        }

        try (
            LHKeyValueIterator<UserTaskRun> iter = localStore.prefixScan(
                prefix,
                UserTaskRun.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<UserTaskRun> next = iter.next();
                getableStorageManager.delete(next.getKey(), UserTaskRun.class);
            }
        }

        try (
            LHKeyValueIterator<TaskRun> iter = localStore.prefixScan(
                prefix,
                TaskRun.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskRun> next = iter.next();
                getableStorageManager.delete(next.getKey(), TaskRun.class);
            }
        }

        try (
            LHKeyValueIterator<ExternalEvent> iter = localStore.prefixScan(
                prefix,
                ExternalEvent.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<ExternalEvent> next = iter.next();
                getableStorageManager.delete(next.getKey(), ExternalEvent.class);
            }
        }
    }

    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;

        try (
            LHKeyValueIterator<ScheduledTask> iter = localStore.prefixScan(
                "",
                ScheduledTask.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTask> next = iter.next();
                ScheduledTask scheduledTask = next.getValue();
                log.debug(
                    "Rehydration: scheduling task: {}",
                    scheduledTask.getStoreKey()
                );
                server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
            }
        }
    }

    private void flush() {
        for (Map.Entry<String, NodeRun> e : nodeRunPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), NodeRun.class);
        }
        for (Map.Entry<String, ExternalEvent> e : extEvtPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), ExternalEvent.class);
        }
        for (Map.Entry<String, WfRun> e : wfRunPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), WfRun.class);
        }
        for (Map.Entry<String, TaskRun> e : taskRunPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), TaskRun.class);
        }
        for (Map.Entry<String, UserTaskRun> e : userTaskRunPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), UserTaskRun.class);
        }
        // Turns out that we have to save the WfRun's before we save the Variable.
        for (Map.Entry<String, Variable> e : variablePuts.entrySet()) {
            Variable v = e.getValue();
            if (v != null) {
                if (v.getWfSpec() == null) {
                    // that's because otherwise the getWfRun() call might be null
                    v.setWfSpec(getWfRun(v.wfRunId).getWfSpec());
                }
            }
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), Variable.class);
        }

        for (Map.Entry<String, TaskWorkerGroup> e : taskWorkerGroupPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), TaskWorkerGroup.class);
        }

        for (Map.Entry<String, ExternalEventDef> e : extEvtDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(
                e.getKey(),
                e.getValue(),
                ExternalEventDef.class
            );
            forwardGlobalMeta(e.getKey(), e.getValue(), ExternalEventDef.class);
        }
        for (Map.Entry<String, WfSpec> e : wfSpecPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), WfSpec.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), WfSpec.class);
        }
        for (Map.Entry<String, UserTaskDef> e : userTaskDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), UserTaskDef.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), UserTaskDef.class);
        }
        for (Map.Entry<String, TaskDef> e : taskDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), TaskDef.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), TaskDef.class);
        }

        for (LHTimer timer : timersToSchedule) {
            forwardTimer(timer);
        }

        for (Map.Entry<String, ScheduledTask> entry : scheduledTaskPuts.entrySet()) {
            String scheduledTaskId = entry.getKey();
            ScheduledTask scheduledTask = entry.getValue();
            if (scheduledTask != null) {
                forwardTask(scheduledTask);
            } else {
                // It's time to delete the thing.
                saveOrDeleteStorableFlush(scheduledTaskId, null, ScheduledTask.class);
            }
        }

        // TODO: Update metrics somewhere

        for (TaskMetricUpdate tmu : taskMetricPuts.values()) {
            localStore.put(tmu);
        }

        for (WfMetricUpdate wmu : wfMetricPuts.values()) {
            localStore.put(wmu);
        }
    }

    public List<WfMetricUpdate> getWfMetricWindows(
        String wfSpecName,
        int wfSpecVersion,
        Date time
    ) {
        List<WfMetricUpdate> out = new ArrayList<>();
        out.add(
            getWmUpdate(
                time,
                MetricsWindowLengthPb.MINUTES_5,
                wfSpecName,
                wfSpecVersion
            )
        );
        out.add(
            getWmUpdate(
                time,
                MetricsWindowLengthPb.HOURS_2,
                wfSpecName,
                wfSpecVersion
            )
        );
        out.add(
            getWmUpdate(time, MetricsWindowLengthPb.DAYS_1, wfSpecName, wfSpecVersion)
        );
        return out;
    }

    private WfMetricUpdate getWmUpdate(
        Date windowStart,
        MetricsWindowLengthPb type,
        String wfSpecName,
        int wfSpecVersion
    ) {
        windowStart = LHUtil.getWindowStart(windowStart, type);
        String id = WfMetricUpdate.getObjectId(
            type,
            windowStart,
            wfSpecName,
            wfSpecVersion
        );
        if (wfMetricPuts.containsKey(id)) {
            return wfMetricPuts.get(id);
        }

        WfMetricUpdate out = localStore.get(id, WfMetricUpdate.class);
        if (out == null) {
            out = new WfMetricUpdate();
            out.windowStart = windowStart;
            out.type = type;
            out.wfSpecName = wfSpecName;
            out.wfSpecVersion = wfSpecVersion;
        }

        wfMetricPuts.put(id, out);
        return out;
    }

    public List<TaskMetricUpdate> getTaskMetricWindows(
        String taskDefName,
        Date time
    ) {
        List<TaskMetricUpdate> out = new ArrayList<>();
        out.add(getTmUpdate(time, MetricsWindowLengthPb.MINUTES_5, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLengthPb.HOURS_2, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLengthPb.DAYS_1, taskDefName));
        return out;
    }

    private TaskMetricUpdate getTmUpdate(
        Date windowStart,
        MetricsWindowLengthPb type,
        String taskDefName
    ) {
        windowStart = LHUtil.getWindowStart(windowStart, type);
        String id = TaskMetricUpdate.getStoreKey(type, windowStart, taskDefName);
        if (taskMetricPuts.containsKey(id)) {
            return taskMetricPuts.get(id);
        }

        TaskMetricUpdate out = localStore.get(id, TaskMetricUpdate.class);
        if (out == null) {
            out = new TaskMetricUpdate();
            out.windowStart = windowStart;
            out.type = type;
            out.taskDefName = taskDefName;
        }

        taskMetricPuts.put(id, out);
        return out;
    }

    private void forwardTask(ScheduledTask scheduledTask) {
        // since tsr is not null, it will save
        saveOrDeleteStorableFlush(
            scheduledTask.getStoreKey(),
            scheduledTask,
            ScheduledTask.class
        );

        // This is where the magic happens
        if (partitionIsClaimed) {
            server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
        } else {
            log.debug("Haven't claimed partitions, deferring scheduling of tsr");
        }
    }

    private void forwardTimer(LHTimer timer) {
        CommandProcessorOutput output = new CommandProcessorOutput(
            config.getTimerTopic(),
            timer,
            timer.key
        );
        ctx.forward(
            new Record<String, CommandProcessorOutput>(
                timer.key,
                output,
                System.currentTimeMillis()
            )
        );
    }

    private <U extends Message, T extends Getable<U>> void forwardGlobalMeta(
        String objectId,
        T val,
        Class<T> cls
    ) {
        // The serializer provided in the sink will produce a tombstone if
        // `val` is null.
        CommandProcessorOutput output = new CommandProcessorOutput(
            config.getGlobalMetadataCLTopicName(),
            val,
            StoreUtils.getFullStoreKey(objectId, cls)
        );
        ctx.forward(
            new Record<String, CommandProcessorOutput>(
                StoreUtils.getFullStoreKey(objectId, cls),
                output,
                System.currentTimeMillis()
            )
        );
    }

    private <
        U extends Message, T extends Storeable<U>
    > void saveOrDeleteStorableFlush(String key, T val, Class<T> cls) {
        if (val != null) {
            localStore.put(val);
        } else {
            T oldThing = localStore.get(key, cls);
            if (oldThing != null) {
                localStore.delete(oldThing);
            } else {
                log.debug("Tried to delete nonexistent {} {}", cls.getName(), key);
            }
        }
    }

    private <U extends Message, T extends Getable<U>> void saveOrDeleteGETableFlush(
        String key,
        T val,
        Class<T> cls
    ) {
        if (val != null) {
            getableStorageManager.store(val);
        } else {
            getableStorageManager.delete(key, cls);
        }
    }

    private void clearThingsToWrite() {
        nodeRunPuts.clear();
        variablePuts.clear();
        extEvtPuts.clear();
        scheduledTaskPuts.clear();
        timersToSchedule.clear();
        wfRunPuts.clear();
        wfSpecPuts.clear();
        taskDefPuts.clear();
        userTaskDefPuts.clear();
        extEvtDefPuts.clear();
        taskWorkerGroupPuts.clear();
        taskMetricPuts = new HashMap<>();
        wfMetricPuts = new HashMap<>();
    }

    public void forwardAndClearMetricsUpdatesUntil() {
        Map<String, TaskMetricUpdate> clusterTaskUpdates = new HashMap<>();

        try (
            LHKeyValueIterator<TaskMetricUpdate> iter = localStore.range(
                "",
                "~",
                TaskMetricUpdate.class
            );
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskMetricUpdate> next = iter.next();

                log.debug("Sending out metrics for {}", next.getKey());

                localStore.delete(next.getKey());
                TaskMetricUpdate tmu = next.getValue();
                forwardTaskMetricUpdate(tmu);

                // Update the cluster-level metrics
                String clusterTaskUpdateKey = TaskMetricUpdate.getStoreKey(
                    tmu.type,
                    tmu.windowStart,
                    LHConstants.CLUSTER_LEVEL_METRIC
                );
                TaskMetricUpdate clusterTaskUpdate;
                if (clusterTaskUpdates.containsKey(clusterTaskUpdateKey)) {
                    clusterTaskUpdate = clusterTaskUpdates.get(clusterTaskUpdateKey);
                } else {
                    clusterTaskUpdate =
                        new TaskMetricUpdate(
                            tmu.windowStart,
                            tmu.type,
                            LHConstants.CLUSTER_LEVEL_METRIC
                        );
                }

                clusterTaskUpdate.merge(tmu);
                clusterTaskUpdates.put(clusterTaskUpdateKey, clusterTaskUpdate);
                localStore.delete(tmu);
            }
        }

        // Forward the cluster level task updates
        for (TaskMetricUpdate tmu : clusterTaskUpdates.values()) {
            forwardTaskMetricUpdate(tmu);
        }

        // get ready to update the cluster level WF Metrics
        Map<String, WfMetricUpdate> clusterWfUpdates = new HashMap<>();

        try (
            LHKeyValueIterator<WfMetricUpdate> iter = localStore.range(
                "",
                "~",
                WfMetricUpdate.class
            );
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<WfMetricUpdate> next = iter.next();
                WfMetricUpdate wmu = next.getValue();
                forwardWfMetricUpdate(wmu);

                // Update the cluster-level metrics
                String clusterWfUpdateKey = WfMetricUpdate.getStoreKey(
                    wmu.type,
                    wmu.windowStart,
                    LHConstants.CLUSTER_LEVEL_METRIC,
                    0
                );
                WfMetricUpdate clusterWfUpdate;
                if (clusterWfUpdates.containsKey(clusterWfUpdateKey)) {
                    clusterWfUpdate = clusterWfUpdates.get(clusterWfUpdateKey);
                } else {
                    clusterWfUpdate =
                        new WfMetricUpdate(
                            wmu.windowStart,
                            wmu.type,
                            LHConstants.CLUSTER_LEVEL_METRIC,
                            0
                        );
                }
                clusterWfUpdate.merge(wmu);
                clusterWfUpdates.put(clusterWfUpdateKey, clusterWfUpdate);

                localStore.delete(wmu);
            }
        }

        // Forward the cluster level task updates
        for (WfMetricUpdate wmu : clusterWfUpdates.values()) {
            forwardWfMetricUpdate(wmu);
        }
    }

    private void forwardTaskMetricUpdate(TaskMetricUpdate tmu) {
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = tmu.getPartitionKey();
        cpo.topic = config.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(tmu, new Date(), tmu.getPartitionKey());
        Record<String, CommandProcessorOutput> out = new Record<>(
            tmu.getPartitionKey(),
            cpo,
            System.currentTimeMillis()
        );
        ctx.forward(out);
    }

    private void forwardWfMetricUpdate(WfMetricUpdate wmu) {
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = wmu.getPartitionKey();
        cpo.topic = config.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(wmu, new Date(), wmu.getPartitionKey());
        Record<String, CommandProcessorOutput> out = new Record<>(
            wmu.getPartitionKey(),
            cpo,
            System.currentTimeMillis()
        );
        ctx.forward(out);
    }

    @Override
    public HostInfoPb getAdvertisedHost(Host host, String listenerName)
        throws LHBadRequestError, LHConnectionError {
        return server.getAdvertisedHost(host, listenerName);
    }

    @Override
    public TaskWorkerGroup getTaskWorkerGroup(String taskDefName) {
        if (taskWorkerGroupPuts.containsKey(taskDefName)) {
            return taskWorkerGroupPuts.get(taskDefName);
        }
        TaskWorkerGroup out = localStore.get(taskDefName, TaskWorkerGroup.class);
        if (out != null) {
            taskWorkerGroupPuts.put(taskDefName, out);
        }
        return out;
    }

    @Override
    public void putTaskWorkerGroup(TaskWorkerGroup taskWorkerGroup) {
        taskWorkerGroupPuts.put(taskWorkerGroup.getStoreKey(), taskWorkerGroup);
    }

    @Override
    public InternalHosts getInternalHosts() {
        Set<Host> newHost = server.getAllInternalHosts();
        InternalHosts internalHosts = new InternalHosts(currentHosts, newHost);
        currentHosts = newHost;
        return internalHosts;
    }
}
