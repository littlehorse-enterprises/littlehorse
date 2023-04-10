package io.littlehorse.server.streamsimpl.coreprocessors;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.ExternalEventDefId;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.model.objectId.WfSpecId;
import io.littlehorse.common.model.observabilityevent.ObservabilityEvent;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.MetricsWindowLengthPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.server.streamsimpl.storeinternals.LHROStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.DiscreteTagLocalCounter;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagChangesToBroadcast;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagUtils;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.log4j.Logger;

public class KafkaStreamsLHDAOImpl implements LHDAO {

    private static final Logger log = Logger.getLogger(KafkaStreamsLHDAOImpl.class);

    private Map<String, NodeRun> nodeRunPuts;
    private Map<String, Variable> variablePuts;
    private Map<String, ExternalEvent> extEvtPuts;
    private Map<String, WfRun> wfRunPuts;
    private Map<String, WfSpec> wfSpecPuts;
    private Map<String, TaskDef> taskDefPuts;
    private Map<String, ExternalEventDef> extEvtDefPuts;
    private Map<String, TaskScheduleRequest> tsrPuts;
    private List<LHTimer> timersToSchedule;
    private CommandResult responseToSave;
    private Command command;
    private int partition;
    private KafkaStreamsServerImpl server;
    private List<ObservabilityEvent> oEvents;
    private Map<String, TaskMetricUpdate> taskMetricPuts;
    private Map<String, WfMetricUpdate> wfMetricPuts;

    private static final String OUTGOING_CHANGELOG_KEY = "OUTGOING_CHANGELOG";

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
    private boolean partitionIsClaimed;

    private Map<String, Pair<Long, WfSpec>> wfSpecCache;
    private Map<String, Pair<Long, TaskDef>> taskDefCache;

    public KafkaStreamsLHDAOImpl(
        final ProcessorContext<String, CommandProcessorOutput> ctx,
        LHConfig config,
        KafkaStreamsServerImpl server
    ) {
        this.server = server;
        this.ctx = ctx;
        this.config = config;

        // At the start, we haven't claimed the partition until the claim event comes
        this.partitionIsClaimed = false;

        nodeRunPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        extEvtPuts = new HashMap<>();
        wfRunPuts = new HashMap<>();
        wfSpecPuts = new HashMap<>();
        extEvtDefPuts = new HashMap<>();
        taskDefPuts = new HashMap<>();
        oEvents = new ArrayList<>();
        taskMetricPuts = new HashMap<>();
        wfMetricPuts = new HashMap<>();

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

        partition = ctx.taskId().partition();

        tsrPuts = new HashMap<>();
        timersToSchedule = new ArrayList<>();

        this.wfSpecCache = new HashMap<>();
        this.taskDefCache = new HashMap<>();
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
        if (out != null) nodeRunPuts.put(key, out);
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

    @Override
    public WfSpec getWfSpec(String name, Integer version) {
        String mapKey = version == null
            ? name
            : new WfSpecId(name, version).getStoreKey();
        Pair<Long, WfSpec> pair = wfSpecCache.get(mapKey);

        if (pair != null && isFreshEnough(pair.getKey())) {
            return pair.getValue();
        }

        WfSpec spec = getWfSpecBreakCache(name, version);
        if (spec != null) {
            wfSpecCache.put(
                spec.getStoreKey(),
                Pair.of(System.currentTimeMillis(), spec)
            );

            Pair<Long, WfSpec> oldOne = wfSpecCache.get(name);

            if (oldOne == null || oldOne.getRight().version < spec.version) {
                wfSpecCache.put(name, Pair.of(System.currentTimeMillis(), spec));
            }
        }
        return spec;
    }

    private WfSpec getWfSpecBreakCache(String name, Integer version) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        if (version != null) {
            return store.get(new WfSpecId(name, version).getStoreKey(), WfSpec.class);
        } else {
            return store.getLastFromPrefix(name, WfSpec.class);
        }
    }

    private boolean isFreshEnough(Long time) {
        return System.currentTimeMillis() - time < (1000 * 60);
    }

    @Override
    public TaskDef getTaskDef(String name) {
        Pair<Long, TaskDef> pair = taskDefCache.get(name);

        if (pair != null && isFreshEnough(pair.getKey())) {
            return pair.getValue();
        }

        TaskDef spec = getTaskDefBreakCache(name);
        if (spec != null) {
            taskDefCache.put(
                spec.getStoreKey(),
                Pair.of(System.currentTimeMillis(), spec)
            );

            Pair<Long, TaskDef> oldOne = taskDefCache.get(name);

            if (oldOne == null) {
                taskDefCache.put(name, Pair.of(System.currentTimeMillis(), spec));
            }
        } else {
            if (pair != null) {
                log.debug(
                    "TaskDef " +
                    name +
                    " was deleted by the API, now deleting from the cache as well"
                );
                taskDefCache.remove(name);
            }
        }
        return spec;
    }

    private TaskDef getTaskDefBreakCache(String name) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        return store.get(new TaskDefId(name).getStoreKey(), TaskDef.class);
    }

    @Override
    public ExternalEventDef getExternalEventDef(String name) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        return store.get(
            new ExternalEventDefId(name).getStoreKey(),
            ExternalEventDef.class
        );
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
        if (out != null) extEvtPuts.put(externalEventId, out);
        return out;
    }

    @Override
    public void saveExternalEvent(ExternalEvent evt) {
        extEvtPuts.put(evt.getStoreKey(), evt);
    }

    @Override
    public void scheduleTask(TaskScheduleRequest tsr) {
        tsrPuts.put(tsr.getStoreKey(), tsr);
    }

    @Override
    public void scheduleTimer(LHTimer timer) {
        timersToSchedule.add(timer);
    }

    @Override
    public TaskScheduleRequest markTaskAsScheduled(
        String wfRunId,
        int threadRunNumber,
        int taskRunPosition
    ) {
        TaskScheduleRequest tsr = localStore.get(
            new NodeRunId(wfRunId, threadRunNumber, taskRunPosition).getStoreKey(),
            TaskScheduleRequest.class
        );

        if (tsr != null) {
            tsrPuts.put(tsr.getStoreKey(), null);
        }

        return tsr;
    }

    @Override
    public WfRun getWfRun(String id) {
        if (wfRunPuts.containsKey(id)) {
            return wfRunPuts.get(id);
        }
        WfRun out = localStore.get(id, WfRun.class);
        if (out != null) wfRunPuts.put(id, out);
        return out;
    }

    @Override
    public void saveWfRun(WfRun wfRun) {
        wfRunPuts.put(wfRun.getStoreKey(), wfRun);
    }

    @Override
    public List<ObservabilityEvent> commitChanges() {
        flush();

        List<ObservabilityEvent> out = oEvents;

        // Now, we clear the saved state. In the future we could have a two-tiered
        // approach in which we have a read cache which saves us from having to
        // query RocksDB and then ALSO deserialize the bytes into the Java object.
        // Potentially one LRU cache for each "object type" would be a good
        // optimization.
        clearThingsToWrite();
        return out;
    }

    @Override
    public void abortChanges() {
        // The contract for this is to cancel any changes. Nothing gets written
        // to rocksdb until commitChanges() successfully returns; therefore,
        // all we have to do is clear the things we were gonna write.
        clearThingsToWrite();
    }

    @Override
    public void abortChangesAndMarkWfRunFailed(String message) {
        for (Map.Entry<String, WfRun> e : wfRunPuts.entrySet()) {
            WfRun affected = e.getValue();
            if (e != null) {
                Failure failure = new Failure(
                    TaskResultCodePb.INTERNAL_ERROR,
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
    public String getWfRunEventQueue() {
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
            taskDefCache.remove(name);
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
            taskDefCache.remove(name);
        }
        return out;
    }

    @Override
    public void addObservabilityEvent(ObservabilityEvent evt) {
        oEvents.add(evt);
    }

    @Override
    public List<ObservabilityEvent> getObservabilityEvents() {
        return oEvents;
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
                deleteThingFlush(next.getKey(), NodeRun.class);
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
                deleteThingFlush(next.getKey(), Variable.class);
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
                deleteThingFlush(next.getKey(), ExternalEvent.class);
            }
        }
    }

    public void onPartitionClaimed() {
        if (partitionIsClaimed) {
            throw new RuntimeException("Re-claiming partition! Yikes!");
        }
        partitionIsClaimed = true;

        try (
            LHKeyValueIterator<TaskScheduleRequest> iter = localStore.prefixScan(
                "",
                TaskScheduleRequest.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskScheduleRequest> next = iter.next();
                TaskScheduleRequest tsr = next.getValue();
                LHUtil.log("Rehydration: scheduling task:", tsr.getStoreKey());
                server.onTaskScheduled(tsr.taskDefName, tsr);
            }
        }
    }

    public void broadcastTagCounts(long time) {
        TagChangesToBroadcast changes = getTagChangesToBroadcast();

        for (Map.Entry<String, DiscreteTagLocalCounter> e : changes.changelog.entrySet()) {
            DiscreteTagLocalCounter c = e.getValue();
            String key = StoreUtils.getFullStoreKey(c);
            CommandProcessorOutput output = new CommandProcessorOutput();
            output.partitionKey = key;
            output.topic = config.getGlobalMetadataCLTopicName();

            if (c.localCount > 0) {
                output.payload = e.getValue();
            } else {
                // tombstone
                output.payload = null;
            }

            ctx.forward(new Record<>(key, output, time));
        }

        // reset the changes; next time tags are put, they will be updated.
        if (!changes.changelog.isEmpty()) {
            localStore.deleteRaw(OUTGOING_CHANGELOG_KEY);
        }
    }

    private void flush() {
        if (responseToSave != null) {
            // TODO: Add a timer to delete the Response in 30 seconds
            localStore.put(responseToSave);
            localStore.putResponseToDelete(responseToSave.getStoreKey());
        }

        for (Map.Entry<String, NodeRun> e : nodeRunPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), NodeRun.class);
        }
        for (Map.Entry<String, ExternalEvent> e : extEvtPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), ExternalEvent.class);
        }
        for (Map.Entry<String, Variable> e : variablePuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), Variable.class);
        }
        for (Map.Entry<String, WfRun> e : wfRunPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), WfRun.class);
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
        for (Map.Entry<String, TaskDef> e : taskDefPuts.entrySet()) {
            saveOrDeleteGETableFlush(e.getKey(), e.getValue(), TaskDef.class);
            forwardGlobalMeta(e.getKey(), e.getValue(), TaskDef.class);
        }

        for (LHTimer timer : timersToSchedule) {
            forwardTimer(timer);
        }

        for (Map.Entry<String, TaskScheduleRequest> entry : tsrPuts.entrySet()) {
            String tsrId = entry.getKey();
            TaskScheduleRequest tsr = entry.getValue();
            if (tsr != null) {
                forwardTask(tsr);
            } else {
                // It's time to delete the thing.
                saveOrDeleteStorableFlush(tsrId, null, TaskScheduleRequest.class);
            }
        }

        // Forward observability events
        for (ObservabilityEvent evt : oEvents) {
            Record<String, CommandProcessorOutput> out = new Record<String, CommandProcessorOutput>(
                command.getPartitionKey(),
                new CommandProcessorOutput(
                    config.getObservabilityEventTopicName(),
                    evt,
                    command.getPartitionKey()
                ),
                System.currentTimeMillis()
            );
            ctx.forward(out);

            evt.updateMetrics(this);
        }

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

    private void forwardTask(TaskScheduleRequest tsr) {
        // since tsr is not null, it will save
        saveOrDeleteStorableFlush(tsr.getStoreKey(), tsr, TaskScheduleRequest.class);

        // This is where the magic happens
        if (partitionIsClaimed) {
            server.onTaskScheduled(tsr.taskDefName, tsr);
        } else {
            LHUtil.log("haven't claimed partitions, deferring scheduling of tsr");
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

    private <U extends Message, T extends GETable<U>> void forwardGlobalMeta(
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
                log.debug("Tried to delete nonexistent " + cls.getName() + " " + key);
            }
        }
    }

    private <U extends Message, T extends GETable<U>> void saveOrDeleteGETableFlush(
        String key,
        T val,
        Class<T> cls
    ) {
        if (val != null) {
            saveAndIndexFlush(val);
        } else {
            deleteThingFlush(key, cls);
        }
    }

    private <U extends Message, T extends GETable<U>> void deleteThingFlush(
        String objectId,
        Class<T> cls
    ) {
        T oldThing = localStore.get(objectId, cls);
        if (oldThing != null) {
            // Delete the old tag cache
            TagsCache cache = localStore.getTagsCache(oldThing);
            for (String tagId : cache.tagIds) {
                deleteTag(tagId);
            }

            localStore.deleteTagCache(oldThing);
            localStore.delete(oldThing);
        } else {
            // Then we know that the object was created and deleted within the same
            // transaction, so we have nothing to do.
            LHUtil.log("Warn: ", cls, objectId, "created and deleted in same txn.");
        }
    }

    private void saveAndIndexFlush(GETable<?> thing) {
        localStore.put(thing);

        TagsCache oldEntriesObj = localStore.getTagsCache(thing);

        List<String> oldTagIds =
            (oldEntriesObj == null ? new ArrayList<>() : oldEntriesObj.tagIds);
        List<String> newTagIds = new ArrayList<>();

        for (Tag newTag : TagUtils.tagThing(thing)) {
            if (!oldTagIds.contains(newTag.getStoreKey())) {
                putTag(newTag);
            }
            newTagIds.add(newTag.getStoreKey());
        }
        for (String oldTagId : oldTagIds) {
            if (!newTagIds.contains(oldTagId)) {
                deleteTag(oldTagId);
            }
        }

        localStore.putTagsCache(thing);
    }

    private void putTag(Tag tag) {
        switch (tag.tagType) {
            case REMOTE_HASH_UNCOUNTED:
                throw new RuntimeException("Remote hash not implemented");
            case LOCAL_HASH_UNCOUNTED:
                throw new RuntimeException("Local hash not implemented");
            case LOCAL_COUNTED:
            case LOCAL_UNCOUNTED:
                // Both of these involve storing the tag on the same partition
                // as the described object.
                localStore.put(tag);
                TagChangesToBroadcast tctb = getTagChangesToBroadcast();

                // If the counter is not null (i.e. LOCAL_COUNTED), then count.
                if (tag.getCounterKey(partition) != null) {
                    DiscreteTagLocalCounter counter = tctb.getCounter(tag);
                    counter.localCount++;
                    localStore.put(counter);
                    localStore.putRaw(
                        OUTGOING_CHANGELOG_KEY,
                        new Bytes(tctb.toBytes(config))
                    );
                }
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }
    }

    private void deleteTag(String tagId) {
        // TODO: Handle the Remote and Hash types.

        Tag tag = localStore.get(tagId, Tag.class);
        localStore.delete(tag);

        if (tag.getCounterKey(partition) != null) {
            TagChangesToBroadcast tctb = getTagChangesToBroadcast();
            DiscreteTagLocalCounter counter = tctb.getCounter(tag);
            counter.localCount--;

            if (counter.localCount >= 0) {
                localStore.put(counter);
            } else {
                localStore.delete(counter);
            }
            localStore.putRaw(
                OUTGOING_CHANGELOG_KEY,
                new Bytes(tctb.toBytes(config))
            );
        }
    }

    private TagChangesToBroadcast getTagChangesToBroadcast() {
        Bytes b = localStore.getRaw(OUTGOING_CHANGELOG_KEY);
        if (b == null) {
            TagChangesToBroadcast out = new TagChangesToBroadcast();
            out.partition = this.partition;
            return out;
        }
        try {
            TagChangesToBroadcast out = LHSerializable.fromBytes(
                b.get(),
                TagChangesToBroadcast.class,
                config
            );
            out.partition = this.partition;
            return out;
        } catch (LHSerdeError exn) {
            // Not Possible unless bug in LittleHorse
            throw new RuntimeException(exn);
        }
    }

    private void clearThingsToWrite() {
        nodeRunPuts.clear();
        variablePuts.clear();
        extEvtPuts.clear();
        tsrPuts.clear();
        timersToSchedule.clear();
        wfRunPuts.clear();
        wfSpecPuts.clear();
        taskDefPuts.clear();
        extEvtDefPuts.clear();
        responseToSave = null;
        oEvents = new ArrayList<>();
        taskMetricPuts = new HashMap<>();
        wfMetricPuts = new HashMap<>();
    }

    public void saveResponse(LHSerializable<?> response, Command command) {
        responseToSave = new CommandResult();
        responseToSave.resultTime = new Date();
        responseToSave.result = response.toBytes(config);
        responseToSave.commandId = command.commandId;
    }

    public void forwardAndClearMetricsUpdatesUntil(long timestamp) {
        // 5-minute window
        clearMetrics(MetricsWindowLengthPb.MINUTES_5);
        clearMetrics(MetricsWindowLengthPb.HOURS_2);
        clearMetrics(MetricsWindowLengthPb.DAYS_1);
    }

    private void clearMetrics(MetricsWindowLengthPb type) {
        /*
         * NOTE: Past versions of this function used to hold the updates until they
         * were "complete". Now, we just send them and we accept that current
         * in-progress windows will have incomplete data. But that's better than
         * having to wait a whole day + grace period to see how many events you've
         * had today (for DAYS_1 window type, for example).
         */

        try (
            LHKeyValueIterator<TaskMetricUpdate> iter = localStore.range(
                "",
                "~",
                TaskMetricUpdate.class
            );
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskMetricUpdate> next = iter.next();
                log.debug("Sending out metrics for " + next.getKey());
                localStore.delete(next.getKey());
                CommandProcessorOutput cpo = new CommandProcessorOutput();
                TaskMetricUpdate tmu = next.getValue();
                cpo.partitionKey = tmu.getPartitionKey();
                cpo.topic = config.getRepartitionTopicName();
                cpo.payload =
                    new RepartitionCommand(tmu, new Date(), tmu.getPartitionKey());
                Record<String, CommandProcessorOutput> out = new Record<>(
                    tmu.getPartitionKey(),
                    cpo,
                    System.currentTimeMillis()
                );
                ctx.forward(out);

                localStore.delete(tmu);
            }
        }

        try (
            LHKeyValueIterator<WfMetricUpdate> iter = localStore.range(
                "",
                "~",
                WfMetricUpdate.class
            );
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<WfMetricUpdate> next = iter.next();
                localStore.delete(next.getKey());
                CommandProcessorOutput cpo = new CommandProcessorOutput();
                WfMetricUpdate tmu = next.getValue();
                cpo.partitionKey = tmu.getPartitionKey();
                cpo.topic = config.getRepartitionTopicName();
                cpo.payload =
                    new RepartitionCommand(tmu, new Date(), tmu.getPartitionKey());
                Record<String, CommandProcessorOutput> out = new Record<>(
                    tmu.getPartitionKey(),
                    cpo,
                    System.currentTimeMillis()
                );
                ctx.forward(out);

                localStore.delete(tmu);
            }
        }
    }
}
