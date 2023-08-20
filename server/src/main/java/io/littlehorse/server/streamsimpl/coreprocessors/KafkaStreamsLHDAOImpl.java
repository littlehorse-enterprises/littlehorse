package io.littlehorse.server.streamsimpl.coreprocessors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
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
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class KafkaStreamsLHDAOImpl implements CoreProcessorDAO {

    private Map<String, WfSpecModel> wfSpecPuts;
    private Map<String, TaskDefModel> taskDefPuts;
    private Map<String, UserTaskDefModel> userTaskDefPuts;
    private Map<String, ExternalEventDefModel> extEvtDefPuts;
    private Map<String, ScheduledTaskModel> scheduledTaskPuts;
    private List<LHTimer> timersToSchedule;
    private CommandModel command;
    private KafkaStreamsServerImpl server;
    private Map<String, TaskMetricUpdate> taskMetricPuts;
    private Map<String, WfMetricUpdate> wfMetricPuts;
    private Set<HostModel> currentHosts;

    /*
     * Certain metadata objects (eg. WfSpec, TaskDef, ExternalEventDef) are "global"
     * in nature. This means three things:
     * 1) Changes to them are low-throughput and infrequent
     * 2) There are a relatively small number of them
     * 3) Other resources (eg. WfRun) need to constantly consult them in order to
     * run.
     *
     * Items 1) and 2) imply that these metadata objects can be stored on one node.
     * Item 3) implies that we *need* every node to have a local copy of these
     * objects.
     *
     * Furthermore, WfSpec's and TaskDef's depend on each other, and we want
     * strongly consistent processing (so that we can't accidentally delete a
     * TaskDef
     * while processing a WfSpec that uses it, for example). Therefore, we want
     * all of the processing to be linearized, and therefore it needs to occur on
     * the same partition.
     *
     * So what do we do? We use the same getParttionKey() for all of the global
     * metadata.
     *
     * The boolean below here is true when this processor owns the "hot" partition.
     */
    private boolean isMetadataProcessorInstance;

    private LHStoreWrapper localStore;
    private LHROStoreWrapper globalStore;
    private ProcessorContext<String, CommandProcessorOutput> ctx;
    private LHConfig config;
    private final WfSpecCache wfSpecCache;
    private boolean partitionIsClaimed;

    private GetableStorageManager storageManager;

    public KafkaStreamsLHDAOImpl(
            final ProcessorContext<String, CommandProcessorOutput> ctx,
            LHConfig config,
            KafkaStreamsServerImpl server,
            WfSpecCache wfSpecCache,
            LHStoreWrapper localStore,
            boolean isMetadataProcessorInstance) {
        this.server = server;
        this.ctx = ctx;
        this.config = config;
        this.wfSpecCache = wfSpecCache;

        // At the start, we haven't claimed the partition until the claim event comes
        this.partitionIsClaimed = false;

        wfSpecPuts = new HashMap<>();
        extEvtDefPuts = new HashMap<>();
        taskDefPuts = new HashMap<>();
        userTaskDefPuts = new HashMap<>();
        taskMetricPuts = new HashMap<>();
        wfMetricPuts = new HashMap<>();

        // Determines if this instance should read Metadata from the global store or
        // from the local
        // store
        this.isMetadataProcessorInstance = isMetadataProcessorInstance;

        ReadOnlyKeyValueStore<String, Bytes> rawGlobalStore = ctx.getStateStore(ServerTopology.GLOBAL_STORE);
        this.localStore = localStore;
        globalStore = new LHROStoreWrapper(rawGlobalStore, config);

        storageManager = new GetableStorageManager(localStore, config, ctx);

        scheduledTaskPuts = new HashMap<>();
        timersToSchedule = new ArrayList<>();
    }

    @Override
    public void putNodeRun(NodeRunModel nr) {
        storageManager.put(nr, NodeRunModel.class);
    }

    @Override
    public NodeRunModel getNodeRun(String wfRunId, int threadNum, int position) {
        String key = new NodeRunIdModel(wfRunId, threadNum, position).getStoreKey();
        NodeRunModel nodeRunModel = storageManager.get(key, NodeRunModel.class);
        if (nodeRunModel != null) {
            nodeRunModel.setDao(this);
        }
        return nodeRunModel;
    }

    @Override
    public void putTaskRun(TaskRunModel tr) {
        storageManager.put(tr, TaskRunModel.class);
    }

    @Override
    public TaskRunModel getTaskRun(TaskRunIdModel taskRunId) {
        String key = taskRunId.getStoreKey();
        TaskRunModel taskRun = storageManager.get(key, TaskRunModel.class);
        if (taskRun != null) {
            taskRun.setDao(this);
        }
        return taskRun;
    }

    @Override
    public void putUserTaskRun(UserTaskRunModel utr) {
        storageManager.put(utr, UserTaskRunModel.class);
    }

    @Override
    public UserTaskRunModel getUserTaskRun(UserTaskRunIdModel userTaskRunId) {
        String key = userTaskRunId.getStoreKey();
        UserTaskRunModel userTaskRun = storageManager.get(key, UserTaskRunModel.class);
        if (userTaskRun != null) {
            userTaskRun.setDao(this);
        }
        return userTaskRun;
    }

    @Override
    public void setCommand(CommandModel command) {
        this.command = command;
    }

    @Override
    public CommandModel getCommand() {
        return this.command;
    }

    @Override
    public void putWfSpec(WfSpecModel spec) {
        if (!isMetadataProcessorInstance) {
            throw new RuntimeException("Tried to put metadata despite being on the wrong partition!");
        }
        wfSpecPuts.put(spec.getStoreKey(), spec);
    }

    @Override
    public void putExternalEventDef(ExternalEventDefModel spec) {
        if (!isMetadataProcessorInstance) {
            throw new RuntimeException("Tried to put metadata despite being on the wrong partition!");
        }
        extEvtDefPuts.put(spec.getStoreKey(), spec);
    }

    @Override
    public void putTaskDef(TaskDefModel spec) {
        if (!isMetadataProcessorInstance) {
            throw new RuntimeException("Tried to put metadata despite being on the wrong partition!");
        }
        taskDefPuts.put(spec.getStoreKey(), spec);
    }

    // TODO: Investigate whether there is a potential issue with
    // Read-Your-Own-Writes if a process() method does:
    //
    // dao.putWfSpec(foo)
    // dao.getWfSpec("foo", null)
    //
    // It would return the last wfSpec version before the one called by put().
    // However, that doesn't happen in the code now; we should file a JIRA to
    // take care of it for later.
    @Override
    public WfSpecModel getWfSpec(String name, Integer version) {
        Supplier<WfSpecModel> findWfSpec = () -> {
            LHROStoreWrapper store = isMetadataProcessorInstance ? localStore : globalStore;
            if (version != null) {
                return store.get(new WfSpecIdModel(name, version).getStoreKey(), WfSpecModel.class);
            }
            return store.getLastFromPrefix(name, WfSpecModel.class);
        };
        WfSpecModel wfSpecModel = wfSpecCache.getOrCache(name, version, findWfSpec);
        if (wfSpecModel != null)
            wfSpecModel.setDao(this);
        return wfSpecModel;
    }

    // TODO: Investigate whether there is a potential issue with
    // Read-Your-Own-Writes if a process() method does:
    //
    // dao.putUserTaskDef(foo)
    // dao.getUsertaskDef("foo", null)
    //
    // It would return the last UserTaskDef version before the one called by put().
    // However, that doesn't happen in the code now; we should file a JIRA to
    // take care of it for later.
    @Override
    public UserTaskDefModel getUserTaskDef(String name, Integer version) {
        LHROStoreWrapper store = isMetadataProcessorInstance ? localStore : globalStore;
        UserTaskDefModel out;
        if (version != null) {
            // First check the most recent puts
            out = store.get(new UserTaskDefIdModel(name, version).getStoreKey(), UserTaskDefModel.class);
        } else {
            out = store.getLastFromPrefix(name, UserTaskDefModel.class);
        }
        if (out != null)
            out.setDao(this);
        return out;
    }

    // Same R-Y-O-W Issue
    @Override
    public void putUserTaskDef(UserTaskDefModel spec) {
        if (!isMetadataProcessorInstance) {
            throw new RuntimeException("Tried to put metadata despite being on the wrong partition!");
        }
        userTaskDefPuts.put(spec.getStoreKey(), spec);
    }

    // Same R-Y-O-W issue
    @Override
    public TaskDefModel getTaskDef(String name) {
        TaskDefModel out = taskDefPuts.get(name);
        if (out != null)
            return out;

        LHROStoreWrapper store = isMetadataProcessorInstance ? localStore : globalStore;
        return store.get(new TaskDefIdModel(name).getStoreKey(), TaskDefModel.class);
    }

    // Same here, same R-Y-O-W issue
    @Override
    public ExternalEventDefModel getExternalEventDef(String name) {
        LHROStoreWrapper store = isMetadataProcessorInstance ? localStore : globalStore;
        ExternalEventDefModel out = store.get(new ExternalEventDefIdModel(name).getStoreKey(),
                ExternalEventDefModel.class);
        if (out != null) {
            out.setDao(this);
        }
        return out;
    }

    @Override
    public ReadOnlyMetadataStore getGlobalMetaStores() {
        return this;
    }

    @Override
    public void putVariable(VariableModel var) {
        storageManager.put(var, VariableModel.class);
    }

    @Override
    public VariableModel getVariable(String wfRunId, String name, int threadNum) {
        String key = new VariableIdModel(wfRunId, threadNum, name).getStoreKey();
        VariableModel variable = storageManager.get(key, VariableModel.class);
        if (variable != null) {
            if (variable.getWfSpecModel() == null) {
                WfRunModel wfRunModel = getWfRun(wfRunId);
                variable.setWfSpecModel(wfRunModel.getWfSpecModel());
            }
            variable.setDao(this);
        }
        return variable;
    }

    @Override
    public ExternalEventModel getUnclaimedEvent(String wfRunId, String externalEventDefName) {
        String extEvtPrefix = ExternalEventModel.getStorePrefix(wfRunId, externalEventDefName);
        return storageManager.getFirstByCreatedTimeFromPrefix(
                extEvtPrefix, ExternalEventModel.class, externalEvent -> !externalEvent.isClaimed());
    }

    @Override
    public ExternalEventModel getExternalEvent(String externalEventId) {
        ExternalEventModel externalEvent = storageManager.get(externalEventId, ExternalEventModel.class);
        if (externalEvent != null) {
            externalEvent.setDao(this);
        }
        return externalEvent;
    }

    @Override
    public void saveExternalEvent(ExternalEventModel evt) {
        storageManager.put(evt, ExternalEventModel.class);
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
        ScheduledTaskModel scheduledTask = localStore.get(taskRunId.getStoreKey(), ScheduledTaskModel.class);

        if (scheduledTask != null) {
            scheduledTaskPuts.put(scheduledTask.getStoreKey(), null);
        }

        return scheduledTask;
    }

    @Override
    public WfRunModel getWfRun(String id) {
        WfRunModel wfRunModel = storageManager.get(id, WfRunModel.class);
        if (wfRunModel != null) {
            wfRunModel.setDao(this);
            wfRunModel.setWfSpecModel(getWfSpec(wfRunModel.wfSpecName, wfRunModel.wfSpecVersion));
        }
        return wfRunModel;
    }

    @Override
    public void saveWfRun(WfRunModel wfRunModel) {
        storageManager.put(wfRunModel, WfRunModel.class);
    }

    @Override
    public void commitChanges() {
        storageManager.commit();
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
    public void abortChangesAndMarkWfRunFailed(Throwable failure, String wfRunId) {
        // if the wfRun exists: we want to mark it as failed with a message.
        // Else, do nothing.
        WfRunModel wfRunModel = storageManager.get(wfRunId, WfRunModel.class);
        if (wfRunModel != null) {
            log.warn("Marking wfRun {} as failed due to internal LH exception", wfRunId);
            ThreadRunModel entrypoint = wfRunModel.getThreadRun(0);
            entrypoint.setStatus(LHStatus.ERROR);

            String message = "Had an internal LH failur processing command of type "
                    + command.getType()
                    + ": "
                    + failure.getMessage();
            entrypoint.setErrorMessage(message);
            storageManager.abortAndUpdate(wfRunModel);
        } else {
            log.warn("Caught internal LH error but found no WfRun with id {}", wfRunId);
        }
        clearThingsToWrite();
    }

    @Override
    public String getCoreCmdTopic() {
        return config.getCoreCmdTopicName();
    }

    @Override
    public DeleteObjectReply deleteWfRun(String wfRunId) {
        WfRunModel wfRunModel = getWfRun(wfRunId);

        if (wfRunModel == null) {
            return new DeleteObjectReply(LHResponseCode.NOT_FOUND_ERROR, "Couldn't find wfRun with provided ID.");
        }

        if (wfRunModel.isRunning()) {
            return new DeleteObjectReply(LHResponseCode.BAD_REQUEST_ERROR, "Specified wfRun is still RUNNING!");
        }

        // By this point it's guaranteed that a wfRun exists
        storageManager.delete(wfRunId, WfRunModel.class);

        deleteAllChildren(wfRunModel);

        return new DeleteObjectReply(LHResponseCode.OK, null);
    }

    @Override
    public DeleteObjectReply deleteTaskDef(String name) {
        TaskDefModel toDelete = getTaskDef(name);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            taskDefPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCode.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteUserTaskDef(String name, int version) {
        UserTaskDefModel toDelete = getUserTaskDef(name, version);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            userTaskDefPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCode.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteWfSpec(String name, int version) {
        WfSpecModel toDelete = getWfSpec(name, version);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            wfSpecPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCode.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteExternalEventDef(String name) {
        ExternalEventDefModel toDelete = getExternalEventDef(name);
        DeleteObjectReply out = new DeleteObjectReply();
        if (toDelete == null) {
            out.code = LHResponseCode.NOT_FOUND_ERROR;
            out.message = "Couldn't find object with provided ID.";
        } else {
            extEvtDefPuts.put(toDelete.getStoreKey(), null);
            out.code = LHResponseCode.OK;
        }
        return out;
    }

    @Override
    public DeleteObjectReply deleteExternalEvent(String externalEventId) {
        ExternalEventModel toDelete = getExternalEvent(externalEventId);
        if (toDelete == null) {
            return new DeleteObjectReply(LHResponseCode.NOT_FOUND_ERROR, "Couldn't find object with provided ID.");
        }
        storageManager.delete(toDelete.getStoreKey(), ExternalEventModel.class);
        return new DeleteObjectReply(LHResponseCode.OK, null);
    }

    /*
     * Delete the following things from the wfRun:
     * - NodeRun
     * - Variable
     * - ExternalEvent
     */
    private void deleteAllChildren(WfRunModel wfRunModel) {
        String prefix = wfRunModel.id;
        try (LHKeyValueIterator<NodeRunModel> iter = localStore.prefixScan(prefix, NodeRunModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<NodeRunModel> next = iter.next();
                storageManager.delete(next.getKey(), NodeRunModel.class);
            }
        }

        try (LHKeyValueIterator<VariableModel> iter = localStore.prefixScan(prefix, VariableModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<VariableModel> next = iter.next();
                storageManager.delete(next.getKey(), VariableModel.class);
            }
        }

        try (LHKeyValueIterator<UserTaskRunModel> iter = localStore.prefixScan(prefix, UserTaskRunModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<UserTaskRunModel> next = iter.next();
                storageManager.delete(next.getKey(), UserTaskRunModel.class);
            }
        }

        try (LHKeyValueIterator<TaskRunModel> iter = localStore.prefixScan(prefix, TaskRunModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskRunModel> next = iter.next();
                storageManager.delete(next.getKey(), TaskRunModel.class);
            }
        }

        try (LHKeyValueIterator<ExternalEventModel> iter = localStore.prefixScan(prefix, ExternalEventModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ExternalEventModel> next = iter.next();
                storageManager.delete(next.getKey(), ExternalEventModel.class);
            }
        }
    }

    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;

        try (LHKeyValueIterator<ScheduledTaskModel> iter = localStore.prefixScan("", ScheduledTaskModel.class)) {
            while (iter.hasNext()) {
                LHIterKeyValue<ScheduledTaskModel> next = iter.next();
                ScheduledTaskModel scheduledTask = next.getValue();
                log.debug("Rehydration: scheduling task: {}", scheduledTask.getStoreKey());
                server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
            }
        }
    }

    private void flush() {
        for (Map.Entry<String, ExternalEventDefModel> e : extEvtDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), ExternalEventDefModel.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), ExternalEventDefModel.class);
        }
        for (Map.Entry<String, WfSpecModel> e : wfSpecPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), WfSpecModel.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), WfSpecModel.class);
        }
        for (Map.Entry<String, UserTaskDefModel> e : userTaskDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), UserTaskDefModel.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), UserTaskDefModel.class);
        }
        for (Map.Entry<String, TaskDefModel> e : taskDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), TaskDefModel.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), TaskDefModel.class);
        }

        for (LHTimer timer : timersToSchedule) {
            forwardTimer(timer);
        }

        for (Map.Entry<String, ScheduledTaskModel> entry : scheduledTaskPuts.entrySet()) {
            String scheduledTaskId = entry.getKey();
            ScheduledTaskModel scheduledTask = entry.getValue();
            if (scheduledTask != null) {
                forwardTask(scheduledTask);
            } else {
                // It's time to delete the thing.
                saveOrDeleteStorableFlush(scheduledTaskId, null, ScheduledTaskModel.class);
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

    public List<WfMetricUpdate> getWfMetricWindows(String wfSpecName, int wfSpecVersion, Date time) {
        List<WfMetricUpdate> out = new ArrayList<>();
        out.add(getWmUpdate(time, MetricsWindowLength.MINUTES_5, wfSpecName, wfSpecVersion));
        out.add(getWmUpdate(time, MetricsWindowLength.HOURS_2, wfSpecName, wfSpecVersion));
        out.add(getWmUpdate(time, MetricsWindowLength.DAYS_1, wfSpecName, wfSpecVersion));
        return out;
    }

    private WfMetricUpdate getWmUpdate(
            Date windowStart, MetricsWindowLength type, String wfSpecName, int wfSpecVersion) {
        windowStart = LHUtil.getWindowStart(windowStart, type);
        String id = WfMetricUpdate.getObjectId(type, windowStart, wfSpecName, wfSpecVersion);
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

    public List<TaskMetricUpdate> getTaskMetricWindows(String taskDefName, Date time) {
        List<TaskMetricUpdate> out = new ArrayList<>();
        out.add(getTmUpdate(time, MetricsWindowLength.MINUTES_5, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLength.HOURS_2, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLength.DAYS_1, taskDefName));
        return out;
    }

    private TaskMetricUpdate getTmUpdate(Date windowStart, MetricsWindowLength type, String taskDefName) {
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

    private void forwardTask(ScheduledTaskModel scheduledTask) {
        // since tsr is not null, it will save
        saveOrDeleteStorableFlush(scheduledTask.getStoreKey(), scheduledTask, ScheduledTaskModel.class);

        // This is where the magic happens
        if (partitionIsClaimed) {
            server.onTaskScheduled(scheduledTask.getTaskDefId(), scheduledTask);
        } else {
            log.debug("Haven't claimed partitions, deferring scheduling of tsr");
        }
    }

    private void forwardTimer(LHTimer timer) {
        CommandProcessorOutput output = new CommandProcessorOutput(config.getTimerTopic(), timer, timer.key);
        ctx.forward(new Record<String, CommandProcessorOutput>(timer.key, output, System.currentTimeMillis()));
    }

    private <U extends Message, T extends AbstractGetable<U>> void forwardGlobalMeta(String objectId, T val,
            Class<T> cls) {
        String fullStoreKey = StoreUtils.getFullStoreKey(objectId, cls);

        // The serializer provided in the sink will produce a tombstone if
        // `val` is null.
        CommandProcessorOutput output = new CommandProcessorOutput(config.getGlobalMetadataCLTopicName(), val,
                fullStoreKey);
        ctx.forward(new Record<String, CommandProcessorOutput>(fullStoreKey, output, System.currentTimeMillis()));
    }

    private <U extends Message, T extends Storeable<U>> void saveOrDeleteStorableFlush(
            String key, T val, Class<T> cls) {
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

    /**
     * @deprecated Should not use this method because it's not saving/deleting using
     *             the
     *             StoredGetable class. This method will be removed once all
     *             entities are migrated to use
     *             the StoredGetable class.
     */
    @Deprecated(forRemoval = true)
    private <U extends Message, T extends AbstractGetable<U>> void saveOrDeleteGETableFlush(String key, T val,
            Class<T> cls) {
        if (val != null) {
            storageManager.store(val);
        } else {
            storageManager.deleteGetable(key, cls);
        }
    }

    private void clearThingsToWrite() {
        scheduledTaskPuts.clear();
        timersToSchedule.clear();
        wfSpecPuts.clear();
        taskDefPuts.clear();
        userTaskDefPuts.clear();
        extEvtDefPuts.clear();
        taskMetricPuts = new HashMap<>();
        wfMetricPuts = new HashMap<>();

        localStore.clearCommandMetrics(getCommand());
    }

    public void forwardAndClearMetricsUpdatesUntil() {
        Map<String, TaskMetricUpdate> clusterTaskUpdates = new HashMap<>();

        try (LHKeyValueIterator<TaskMetricUpdate> iter = localStore.range("", "~", TaskMetricUpdate.class);) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskMetricUpdate> next = iter.next();

                log.debug("Sending out metrics for {}", next.getKey());

                localStore.delete(next.getKey());
                TaskMetricUpdate tmu = next.getValue();
                forwardTaskMetricUpdate(tmu);

                // Update the cluster-level metrics
                String clusterTaskUpdateKey = TaskMetricUpdate.getStoreKey(tmu.type, tmu.windowStart,
                        LHConstants.CLUSTER_LEVEL_METRIC);
                TaskMetricUpdate clusterTaskUpdate;
                if (clusterTaskUpdates.containsKey(clusterTaskUpdateKey)) {
                    clusterTaskUpdate = clusterTaskUpdates.get(clusterTaskUpdateKey);
                } else {
                    clusterTaskUpdate = new TaskMetricUpdate(tmu.windowStart, tmu.type,
                            LHConstants.CLUSTER_LEVEL_METRIC);
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

        try (LHKeyValueIterator<WfMetricUpdate> iter = localStore.range("", "~", WfMetricUpdate.class);) {
            while (iter.hasNext()) {
                LHIterKeyValue<WfMetricUpdate> next = iter.next();
                WfMetricUpdate wmu = next.getValue();
                forwardWfMetricUpdate(wmu);

                // Update the cluster-level metrics
                String clusterWfUpdateKey = WfMetricUpdate.getStoreKey(wmu.type, wmu.windowStart,
                        LHConstants.CLUSTER_LEVEL_METRIC, 0);
                WfMetricUpdate clusterWfUpdate;
                if (clusterWfUpdates.containsKey(clusterWfUpdateKey)) {
                    clusterWfUpdate = clusterWfUpdates.get(clusterWfUpdateKey);
                } else {
                    clusterWfUpdate = new WfMetricUpdate(wmu.windowStart, wmu.type, LHConstants.CLUSTER_LEVEL_METRIC,
                            0);
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
        Record<String, CommandProcessorOutput> out = new Record<>(tmu.getPartitionKey(), cpo,
                System.currentTimeMillis());
        ctx.forward(out);
    }

    private void forwardWfMetricUpdate(WfMetricUpdate wmu) {
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = wmu.getPartitionKey();
        cpo.topic = config.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(wmu, new Date(), wmu.getPartitionKey());
        Record<String, CommandProcessorOutput> out = new Record<>(wmu.getPartitionKey(), cpo,
                System.currentTimeMillis());
        ctx.forward(out);
    }

    @Override
    public HostInfo getAdvertisedHost(HostModel host, String listenerName) throws LHBadRequestError, LHConnectionError {
        return server.getAdvertisedHost(host, listenerName);
    }

    @Override
    public TaskWorkerGroupModel getTaskWorkerGroup(String taskDefName) {
        TaskWorkerGroupModel taskWorkerGroup = storageManager.get(taskDefName, TaskWorkerGroupModel.class);
        if (taskWorkerGroup != null) {
            taskWorkerGroup.setDao(this);
        }
        return taskWorkerGroup;
    }

    @Override
    public void putTaskWorkerGroup(TaskWorkerGroupModel taskWorkerGroup) {
        storageManager.put(taskWorkerGroup, TaskWorkerGroupModel.class);
    }

    @Override
    public InternalHosts getInternalHosts() {
        Set<HostModel> newHost = server.getAllInternalHosts();
        InternalHosts internalHosts = new InternalHosts(currentHosts, newHost);
        currentHosts = newHost;
        return internalHosts;
    }
}
