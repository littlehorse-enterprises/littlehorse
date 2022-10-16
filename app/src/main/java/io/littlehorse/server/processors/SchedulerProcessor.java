package io.littlehorse.server.processors;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.processors.util.GenericOutput;
import io.littlehorse.server.processors.util.WfRunStoreAccess;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class SchedulerProcessor
    implements Processor<String, WfRunEvent, String, GenericOutput> {

    private KeyValueStore<String, WfRun> wfRunStore;
    private Map<String, WfSpec> wfSpecCache;
    private ProcessorContext<String, GenericOutput> context;
    private ReadOnlyKeyValueStore<String, WfSpec> wfSpecStore;
    private KeyValueStore<String, NodeRun> nodeRunStore;
    private KeyValueStore<String, Variable> variableStore;
    private ReadOnlyKeyValueStore<String, TaskDef> taskDefStore;
    private ReadOnlyKeyValueStore<String, ExternalEventDef> eedStore;
    private KeyValueStore<String, ExternalEvent> extEvtStore;

    public SchedulerProcessor(LHConfig config) {}

    @Override
    public void init(final ProcessorContext<String, GenericOutput> context) {
        wfRunStore = context.getStateStore(GETable.getBaseStoreName(WfRun.class));
        wfSpecStore =
            context.getStateStore(GlobalPOSTable.getGlobalStoreName(WfSpec.class));
        nodeRunStore = context.getStateStore(GETable.getBaseStoreName(NodeRun.class));
        variableStore =
            context.getStateStore(GETable.getBaseStoreName(Variable.class));
        taskDefStore =
            context.getStateStore(GlobalPOSTable.getGlobalStoreName(TaskDef.class));
        eedStore =
            context.getStateStore(
                GlobalPOSTable.getGlobalStoreName(ExternalEventDef.class)
            );
        extEvtStore =
            context.getStateStore(GETable.getBaseStoreName(ExternalEvent.class));
        this.context = context;
        this.wfSpecCache = new HashMap<>();
    }

    @Override
    public void process(final Record<String, WfRunEvent> record) {
        if (record.value() == null) {
            // Then it's a null event which is used simply to tick up the stream time for the
            // timer clearing method.
            return;
        }
        safeProcess(record.key(), record.timestamp(), record.value());
    }

    private void safeProcess(String key, long timestamp, WfRunEvent value) {
        try {
            processHelper(key, timestamp, value);
        } catch (Exception exn) {
            String wfRunId = key;
            WfRun wfRun = wfRunStore.get(wfRunId);
            if (wfRun == null) {
                exn.printStackTrace();
                return;
            }
            try {
                exn.printStackTrace();
                wfRun.status = LHStatusPb.ERROR;
                LHUtil.log(
                    "Bad WFRun, putting in ERROR. TODO: add a FailureMessage to WFRun",
                    value.type
                );
                wfRunStore.put(wfRunId, wfRun);
            } catch (Exception exn2) {
                exn2.printStackTrace();
            }
        }
    }

    // NOTE: the wfRunId is also just the key from the Kafka record.
    private void processHelper(String wfRunID, long timestamp, WfRunEvent evt) {
        WfRun wfRun = wfRunStore.get(wfRunID);
        WfSpec spec = getWfSpec(evt.wfSpecId);
        // Both `wfRun` and `spec` could be null at this point:
        // `spec` is null if it's an ExternalEvent
        // `wfRun` is null if it's a WfRunRequest or an ExternalEvent that comes
        //   before the associated WfRun.

        WfRunStoreAccess wsa = new WfRunStoreAccess(
            nodeRunStore,
            variableStore,
            extEvtStore,
            wfRunID
        );
        if (evt.type == EventCase.EXTERNAL_EVENT) {
            ExternalEvent extEvt = evt.externalEvent;
            wsa.saveExternalEvent(extEvt);
            flushChanges(wfRun, wsa, timestamp);

            if (wfRun == null) {
                // Then it's a potential future WfRun. Current implementation saves
                // the ExternalEvent in case the WfRun comes through later, enabling
                // joins for "late-arriving" WfRuns.
                //
                // The problem with that approach is that it can lead to a lot of data
                // accumulating and never going anywhere. May need to implement some
                // cleanup policies. Options are:
                // 1. Disallow the saving of ExternalEvents beforehand (not likely)
                // 2. Set a separate retention period for orphan ExternalEvents
                //    - Normally, cleanup would happen when we do cleanup of WfRun
                //      but this is a separate and special case.
                return;
            }

            // ExternalEvent's don't have the wfSpecId on them set by default
            // when they're sent through the API. This is a hack that solves it.
            evt.wfSpecId = wfRun.wfSpecId;
            spec = getWfSpec(evt.wfSpecId);
        } else if (evt.type == EventCase.RUN_REQUEST) {
            if (wfRun != null) {
                LHUtil.log("Got a past run for id " + wfRunID + ", skipping");
                return;
            }
            wfRun = spec.startNewRun(evt, wsa);
        }

        // now `spec` can't be null.
        if (spec == null) {
            LHUtil.log("Couldn't find spec, TODO: DeadLetter Queue");
            return;
        }

        wfRun.wfSpec = spec;
        wfRun.stores = wsa;
        wfRun.processEvent(evt);

        flushChanges(wfRun, wsa, timestamp);
    }

    private void flushChanges(WfRun wfRun, WfRunStoreAccess wsa, long timestamp) {
        // Schedule tasks
        for (TaskScheduleRequest r : wsa.tasksToSchedule) {
            GenericOutput taskOutput = new GenericOutput();
            taskOutput.request = r;
            context.forward(
                new Record<>(wfRun.id, taskOutput, timestamp),
                ServerTopology.schedulerTaskSink
            );
        }

        // Schedule timers
        for (LHTimer timer : wsa.timersToSchedule) {
            GenericOutput out = new GenericOutput();
            out.timer = timer;
            context.forward(
                new Record<>(wfRun.id, out, timestamp),
                ServerTopology.newTimerSink
            );
        }

        // Update the Data Store and send objects to be Tagged+Indexed:

        // Variables
        for (Map.Entry<String, Variable> entry : wsa.variablePuts.entrySet()) {
            variableStore.put(entry.getKey(), entry.getValue());
            forwardforTagging(entry.getValue(), timestamp);
        }

        // TaskRuns
        for (Map.Entry<String, NodeRun> entry : wsa.nodeRunPuts.entrySet()) {
            nodeRunStore.put(entry.getKey(), entry.getValue());
            forwardforTagging(entry.getValue(), timestamp);
        }

        // ExternalEvents
        for (Map.Entry<String, ExternalEvent> entry : wsa.extEvtPuts.entrySet()) {
            extEvtStore.put(entry.getKey(), entry.getValue());
            forwardforTagging(entry.getValue(), timestamp);
        }

        // The WfRun
        if (wfRun != null) {
            wfRunStore.put(wfRun.id, wfRun);
            forwardforTagging(wfRun, timestamp);
        }
    }

    @SuppressWarnings("unchecked")
    private void forwardforTagging(GETable<?> out, long timestamp) {
        GenericOutput varOutput = new GenericOutput();
        varOutput.thingToTag = out;

        Record<String, GenericOutput> rec = new Record<>(
            varOutput.thingToTag.getPartitionKey(),
            varOutput,
            timestamp
        );
        rec.headers().add(LHConstants.OBJECT_ID_HEADER, out.getObjectId().getBytes());

        context.forward(
            rec,
            GETable.getTaggingProcessorName((Class<GETable<?>>) out.getClass())
        );
    }

    private WfSpec getWfSpec(String id) {
        if (id == null) return null;

        WfSpec out = wfSpecCache.get(id);
        if (out == null) {
            out = wfSpecStore.get(id);
            addMetaDependencies(taskDefStore, eedStore, out);
        }
        return out;
    }

    private void addMetaDependencies(
        ReadOnlyKeyValueStore<String, TaskDef> taskDefStore,
        ReadOnlyKeyValueStore<String, ExternalEventDef> externalEventDefStore,
        WfSpec spec
    ) {
        for (ThreadSpec thread : spec.threadSpecs.values()) {
            for (Node node : thread.nodes.values()) {
                if (node.type == NodeCase.TASK) {
                    node.taskNode.taskDef =
                        taskDefStore.get(node.taskNode.taskDefName);
                } else if (node.type == NodeCase.EXTERNAL_EVENT) {
                    node.externalEventNode.externalEventDef =
                        externalEventDefStore.get(
                            node.externalEventNode.externalEventDefName
                        );
                }
            }
        }
    }
}
/*
 * Things to test/fix for Threads:
 * 1. When parent thread fails, child thread should HALT.
 * 2. When child thread fails, parent should continue until EXIT node is reached,
 *    and then go to ERROR state with a message stating that child died.
 *
 * Things to test for Interrupts:
 * 1. Simple test, one thread, lifecycle works.
 * 2. Interrupting Parent thread causes child thread to stop.
 * 3. One interrupt event correctly interrupts two threads which are each
 *    sensitive to that ExternalEventDef, creating two child interrupt threads.
 * 4. A thread that has is already COMPLETED or ERROR is not interrupted.
 * 5. Interrupting child does not affect the parent.
 * 6. Failure to start child interrupt thread causes parent to immediately die.
 * 7. Child interrupt thread failing during execution causes parent to fail
 *    immediately due to CHILD_FAILED.
 * 8. Can interrupt a node that's waiting on a different external event.
 */
