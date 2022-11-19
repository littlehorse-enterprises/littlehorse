package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.server.IndexEntryAction;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.server.Tags;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.IndexActionEnum;
import io.littlehorse.server.CommandProcessorDao;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHLocalStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class CoreServerProcessorDaoImpl implements CommandProcessorDao {

    private Map<String, NodeRun> nodeRunPuts;
    private Map<String, Variable> variablePuts;
    private Map<String, ExternalEvent> extEvtPuts;
    private Map<String, WfRun> wfRunPuts;
    private List<TaskScheduleRequest> tasksToSchedule;
    private List<LHTimer> timersToSchedule;
    private CommandResult responseToSave;

    private LHLocalStore store;
    private ProcessorContext<String, CoreServerProcessorOutput> ctx;
    private LHConfig config;

    public CoreServerProcessorDaoImpl(
        final ProcessorContext<String, CoreServerProcessorOutput> ctx,
        LHConfig config
    ) {
        nodeRunPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        extEvtPuts = new HashMap<>();
        wfRunPuts = new HashMap<>();

        KeyValueStore<String, Bytes> rawStore = ctx.getStateStore(
            ServerTopology.coreStore
        );
        store = new LHLocalStore(rawStore, config);
        this.ctx = ctx;
        this.config = config;

        tasksToSchedule = new ArrayList<>();
        timersToSchedule = new ArrayList<>();
    }

    @Override
    public void putNodeRun(NodeRun nr) {
        nodeRunPuts.put(nr.getObjectId(), nr);
    }

    @Override
    public NodeRun getNodeRun(String wfRunId, int threadNum, int position) {
        String key = NodeRun.getStoreKey(wfRunId, threadNum, position);
        NodeRun out = nodeRunPuts.get(key);
        if (out == null) {
            out = store.get(key, NodeRun.class);
            // Little trick so that if it gets modified it is automatically saved
            if (out != null) nodeRunPuts.put(key, out);
        }
        return out;
    }

    @Override
    public void putVariable(Variable var) {
        variablePuts.put(var.getStoreSubKey(), var);
    }

    @Override
    public Variable getVariable(String wfRunId, String name, int threadNum) {
        String key = Variable.getObjectId(wfRunId, threadNum, name);
        Variable out = variablePuts.get(key);
        if (out == null) {
            out = store.get(key, Variable.class);
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

        // TODO: This is O(N) for number of events correlated with the WfRun.
        // Generally that will only be a small number, but there could be weird
        // use-cases where this could take a long time (if there's 1000 events or
        // so then it could take seconds, which holds up the entire scheduling).
        ExternalEvent out = null;
        try (
            LHKeyValueIterator<ExternalEvent> iter = store.prefixScan(
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
        ExternalEvent out = store.get(externalEventId, ExternalEvent.class);
        if (out != null) extEvtPuts.put(externalEventId, out);
        return out;
    }

    @Override
    public void saveExternalEvent(ExternalEvent evt) {
        extEvtPuts.put(evt.getObjectId(), evt);
    }

    @Override
    public void scheduleTask(TaskScheduleRequest tsr) {
        tasksToSchedule.add(tsr);
    }

    @Override
    public void scheduleTimer(LHTimer timer) {
        timersToSchedule.add(timer);
    }

    @Override
    public WfRun getWfRun(String id) {
        WfRun out = wfRunPuts.get(id);
        if (out != null) {
            return out;
        }
        out = store.get(id, WfRun.class);
        wfRunPuts.put(id, out);
        return out;
    }

    @Override
    public void saveWfRun(WfRun wfRun) {
        wfRunPuts.put(wfRun.getObjectId(), wfRun);
    }

    @Override
    public void commitChanges() {
        flush();

        // Now, we clear the saved state. In the future we could have a two-tiered
        // approach in which we have a read cache which saves us from having to
        // query RocksDB and then ALSO deserialize the bytes into the Java object.
        // Potentially one LRU cache for each "object type" would be a good
        // optimization.
        clearThingsToWrite();
    }

    @Override
    public void abortChanges() {
        // The contract for this is to cancel any changes. Nothing gets written
        // to rocksdb until commitChanges() successfully returns; therefore,
        // all we have to do is clear the things we were gonna write.
        clearThingsToWrite();
    }

    @Override
    public String getWfRunEventQueue() {
        return config.getTagCmdTopic();
    }

    private void flush() {
        if (responseToSave != null) {
            // TODO: Add a timer to delete the Response in 30 seconds
            store.put(responseToSave);
        }

        for (NodeRun nodeRun : nodeRunPuts.values()) {
            saveAndIndexGETable(nodeRun);
        }
        for (ExternalEvent ee : extEvtPuts.values()) {
            saveAndIndexGETable(ee);
        }
        for (Variable v : variablePuts.values()) {
            saveAndIndexGETable(v);
        }
        for (WfRun w : wfRunPuts.values()) {
            saveAndIndexGETable(w);
        }

        for (LHTimer timer : timersToSchedule) {
            forwardTimer(timer);
        }

        for (TaskScheduleRequest tsr : tasksToSchedule) {
            forwardTask(tsr);
        }
    }

    private void forwardTask(TaskScheduleRequest tsr) {
        TaskDef taskDef = store.get(tsr.taskDefId, TaskDef.class);
        CoreServerProcessorOutput output = new CoreServerProcessorOutput(
            taskDef.queueName,
            tsr,
            tsr.wfRunId
        );
        ctx.forward(
            new Record<String, CoreServerProcessorOutput>(
                tsr.wfRunId,
                output,
                System.currentTimeMillis()
            )
        );
    }

    private void forwardTimer(LHTimer timer) {
        CoreServerProcessorOutput output = new CoreServerProcessorOutput(
            config.getTimerTopic(),
            timer,
            timer.key
        );
        ctx.forward(
            new Record<String, CoreServerProcessorOutput>(
                timer.key,
                output,
                System.currentTimeMillis()
            )
        );
    }

    private void saveAndIndexGETable(GETable<?> thing) {
        store.put(thing);

        // We keep a local copy of all of the Tags for the entity.
        // If the tags for an entity change (eg. remove the "STATUS: RUNNING" tag
        // from a WfRun when it completes), we want to be able to delete the old
        // ones. Therefore, we need to keep a record of what they were so we can
        // send "delete tag" requests.

        Tags oldEntriesObj = store.getTagsCache(thing);

        List<Tag> oldIdx =
            (oldEntriesObj == null ? new ArrayList<>() : oldEntriesObj.entries);
        List<Tag> newIdx = thing.getTags();

        for (Tag newTag : newIdx) {
            if (!oldIdx.contains(newTag)) {
                IndexEntryAction action = new IndexEntryAction();
                action.action = IndexActionEnum.CREATE_IDX_ENTRY;
                action.indexEntry = newTag;
                CoreServerProcessorOutput output = new CoreServerProcessorOutput(
                    config.getTagCmdTopic(),
                    action,
                    newTag.getPartitionKey()
                );

                Record<String, CoreServerProcessorOutput> rec = new Record<>(
                    newTag.getPartitionKey(),
                    output,
                    newTag.createdAt.getTime()
                );
                ctx.forward(rec);
            }
        }
        for (Tag oldTag : oldIdx) {
            if (!newIdx.contains(oldTag)) {
                IndexEntryAction action = new IndexEntryAction();
                action.action = IndexActionEnum.DELETE_IDX_ENTRY;
                action.indexEntry = oldTag;

                CoreServerProcessorOutput output = new CoreServerProcessorOutput(
                    config.getTagCmdTopic(),
                    action,
                    oldTag.getPartitionKey()
                );

                ctx.forward(
                    new Record<>(
                        oldTag.getPartitionKey(),
                        output,
                        oldTag.createdAt.getTime()
                    )
                );
            }
        }

        store.putTagsCache(thing);
    }

    private void clearThingsToWrite() {
        nodeRunPuts.clear();
        variablePuts.clear();
        extEvtPuts.clear();
        tasksToSchedule.clear();
        timersToSchedule.clear();
        wfRunPuts.clear();
        responseToSave = null;
    }

    public void saveResponse(LHSerializable<?> response, Command command) {
        responseToSave = new CommandResult();
        responseToSave.resultTime = new Date();
        responseToSave.result = response.toBytes(config);
        responseToSave.commandId = command.commandId;
    }
}
