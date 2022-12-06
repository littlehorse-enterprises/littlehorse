package io.littlehorse.server.streamsbackend.coreserver;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.server.CommandProcessorDao;
import io.littlehorse.server.streamsbackend.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.LHROStoreWrapper;
import io.littlehorse.server.streamsbackend.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsbackend.storeinternals.index.DiscreteTagLocalCounter;
import io.littlehorse.server.streamsbackend.storeinternals.index.Tag;
import io.littlehorse.server.streamsbackend.storeinternals.index.TagChangesToBroadcast;
import io.littlehorse.server.streamsbackend.storeinternals.index.TagUtils;
import io.littlehorse.server.streamsbackend.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsbackend.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class CommandProcessorDaoImpl implements CommandProcessorDao {

    private Map<String, NodeRun> nodeRunPuts;
    private Map<String, Variable> variablePuts;
    private Map<String, ExternalEvent> extEvtPuts;
    private Map<String, WfRun> wfRunPuts;
    private Map<String, WfSpec> wfSpecPuts;
    private Map<String, TaskDef> taskDefPuts;
    private Map<String, ExternalEventDef> extEvtDefPuts;
    private List<TaskScheduleRequest> tasksToSchedule;
    private List<LHTimer> timersToSchedule;
    private CommandResult responseToSave;
    private Command command;
    private int partition;

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

    public CommandProcessorDaoImpl(
        final ProcessorContext<String, CommandProcessorOutput> ctx,
        LHConfig config
    ) {
        nodeRunPuts = new HashMap<>();
        variablePuts = new HashMap<>();
        extEvtPuts = new HashMap<>();
        wfRunPuts = new HashMap<>();
        wfSpecPuts = new HashMap<>();
        extEvtDefPuts = new HashMap<>();
        taskDefPuts = new HashMap<>();

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
        this.ctx = ctx;
        this.config = config;

        partition = ctx.taskId().partition();

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
            out = localStore.get(key, NodeRun.class);
            // Little trick so that if it gets modified it is automatically saved
            if (out != null) nodeRunPuts.put(key, out);
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
        wfSpecPuts.put(spec.getObjectId(), spec);
    }

    @Override
    public void putExternalEventDef(ExternalEventDef spec) {
        if (!isHotMetadataPartition) {
            throw new RuntimeException(
                "Tried to put metadata despite being on the wrong partition!"
            );
        }
        extEvtDefPuts.put(spec.getObjectId(), spec);
    }

    @Override
    public void putTaskDef(TaskDef spec) {
        if (!isHotMetadataPartition) {
            throw new RuntimeException(
                "Tried to put metadata despite being on the wrong partition!"
            );
        }
        taskDefPuts.put(spec.getObjectId(), spec);
    }

    @Override
    public WfSpec getWfSpec(String name, Integer version) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        if (version != null) {
            return store.get(WfSpec.getSubKey(name, version), WfSpec.class);
        } else {
            return store.getLastFromPrefix(name, WfSpec.class);
        }
    }

    @Override
    public TaskDef getTaskDef(String name, Integer version) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        if (version != null) {
            return store.get(TaskDef.getSubKey(name, version), TaskDef.class);
        } else {
            return store.getLastFromPrefix(name, TaskDef.class);
        }
    }

    @Override
    public ExternalEventDef getExternalEventDef(String name, Integer version) {
        LHROStoreWrapper store = isHotMetadataPartition ? localStore : globalStore;
        if (version != null) {
            return store.get(
                ExternalEventDef.getSubKey(name, version),
                ExternalEventDef.class
            );
        } else {
            return store.getLastFromPrefix(name, ExternalEventDef.class);
        }
    }

    @Override
    public LHGlobalMetaStores getGlobalMetaStores() {
        return this;
    }

    @Override
    public void putVariable(Variable var) {
        variablePuts.put(var.getObjectId(), var);
    }

    @Override
    public Variable getVariable(String wfRunId, String name, int threadNum) {
        String key = Variable.getStoreKey(wfRunId, threadNum, name);
        Variable out = variablePuts.get(key);
        if (out == null) {
            out = localStore.get(key, Variable.class);
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
        out = localStore.get(id, WfRun.class);
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
        return config.getCoreCmdTopicName();
    }

    public void broadcastChanges(long time) {
        TagChangesToBroadcast changes = getTagChangesToBroadcast();

        for (Map.Entry<String, DiscreteTagLocalCounter> e : changes.changelog.entrySet()) {
            DiscreteTagLocalCounter c = e.getValue();
            CommandProcessorOutput output = new CommandProcessorOutput();
            output.partitionKey = e.getKey();
            output.topic = config.getGlobalMetadataCLTopicName();

            if (c.localCount > 0) {
                output.payload = e.getValue();
            } else {
                // tombstone
                output.payload = null;
            }

            ctx.forward(new Record<>(output.partitionKey, output, time));
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

        for (ExternalEventDef eed : extEvtDefPuts.values()) {
            saveAndIndexGETable(eed);
            forwardGlobalMeta(eed);
        }
        for (WfSpec ws : wfSpecPuts.values()) {
            saveAndIndexGETable(ws);
            forwardGlobalMeta(ws);
        }
        for (TaskDef td : taskDefPuts.values()) {
            saveAndIndexGETable(td);
            forwardGlobalMeta(td);
        }

        for (LHTimer timer : timersToSchedule) {
            forwardTimer(timer);
        }

        for (TaskScheduleRequest tsr : tasksToSchedule) {
            forwardTask(tsr);
        }
    }

    private void forwardTask(TaskScheduleRequest tsr) {
        TaskDef taskDef = getTaskDef(tsr.taskDefName, null);
        CommandProcessorOutput output = new CommandProcessorOutput(
            taskDef.queueName,
            tsr,
            tsr.wfRunId
        );
        ctx.forward(
            new Record<String, CommandProcessorOutput>(
                tsr.wfRunId,
                output,
                System.currentTimeMillis()
            )
        );
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

    private void forwardGlobalMeta(GETable<?> thing) {
        CommandProcessorOutput output = new CommandProcessorOutput(
            config.getGlobalMetadataCLTopicName(),
            thing,
            StoreUtils.getFullStoreKey(thing)
        );
        ctx.forward(
            new Record<String, CommandProcessorOutput>(
                StoreUtils.getFullStoreKey(thing),
                output,
                System.currentTimeMillis()
            )
        );
    }

    private void saveAndIndexGETable(GETable<?> thing) {
        localStore.put(thing);

        // See `proto/tags.proto` for a detailed description of how tags work.
        TagsCache oldEntriesObj = localStore.getTagsCache(thing);

        List<String> oldTagIds =
            (oldEntriesObj == null ? new ArrayList<>() : oldEntriesObj.tagIds);
        List<String> newTagIds = new ArrayList<>();

        for (Tag newTag : TagUtils.tagThing(thing)) {
            if (!oldTagIds.contains(newTag.getObjectId())) {
                putTag(newTag);
            }
            newTagIds.add(newTag.getObjectId());
        }
        for (String oldTagId : oldTagIds) {
            if (!newTagIds.contains(oldTagId)) {
                deleteTag(oldTagId);
            }
        }

        localStore.putTagsCache(thing);
    }

    private void putTag(Tag tag) {
        localStore.put(tag);
        TagChangesToBroadcast tctb = getTagChangesToBroadcast();

        DiscreteTagLocalCounter counter = tctb.getCounter(tag.getTagAttributes());
        counter.localCount++;

        localStore.put(counter);
        localStore.putRaw(OUTGOING_CHANGELOG_KEY, new Bytes(tctb.toBytes(config)));
    }

    private void deleteTag(String tagId) {
        Tag tag = localStore.get(tagId, Tag.class);
        localStore.delete(tag);
        TagChangesToBroadcast tctb = getTagChangesToBroadcast();

        DiscreteTagLocalCounter counter = tctb.getCounter(tag.getTagAttributes());
        counter.localCount--;

        if (counter.localCount >= 0) {
            localStore.put(counter);
        } else {
            localStore.delete(counter);
        }

        localStore.putRaw(OUTGOING_CHANGELOG_KEY, new Bytes(tctb.toBytes(config)));
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
        tasksToSchedule.clear();
        timersToSchedule.clear();
        wfRunPuts.clear();
        wfSpecPuts.clear();
        taskDefPuts.clear();
        extEvtDefPuts.clear();
        responseToSave = null;
    }

    public void saveResponse(LHSerializable<?> response, Command command) {
        responseToSave = new CommandResult();
        responseToSave.resultTime = new Date();
        responseToSave.result = response.toBytes(config);
        responseToSave.commandId = command.commandId;
    }
}
