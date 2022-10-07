package io.littlehorse.server.processors;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.noderun.NodeRun;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.processors.util.GenericOutput;
import io.littlehorse.server.processors.util.WfRunStoreAccess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    "Bad WFRun, putting in ERROR. TODO: add a FailureMessage to WFRun"
                );
                wfRunStore.put(wfRunId, wfRun);
            } catch (Exception exn2) {
                exn2.printStackTrace();
            }
        }
    }

    private void processHelper(String key, long timestamp, WfRunEvent evt) {
        WfSpec spec = getWfSpec(evt.wfSpecId);
        if (spec == null) {
            LHUtil.log("Couldn't find spec, TODO: DeadLetter Queue");
            return;
        }

        WfRun wfRun = wfRunStore.get(key);

        List<TaskScheduleRequest> tasksToSchedule = new ArrayList<>();
        List<LHTimer> timersToSchedule = new ArrayList<>();
        WfRunStoreAccess wsa = new WfRunStoreAccess(
            nodeRunStore,
            variableStore,
            extEvtStore,
            key
        );

        if (evt.type == EventCase.RUN_REQUEST) {
            if (wfRun != null) {
                LHUtil.log("Got a past run for id " + key + ", skipping");
                return;
            }

            wfRun = spec.startNewRun(evt, tasksToSchedule, timersToSchedule, wsa);
        } else if (evt.type == EventCase.EXTERNAL_EVENT) {
            // Need to save the ExternalEvent payload.
            ExternalEvent extEvt = evt.externalEvent;
            extEvtStore.put(extEvt.getObjectId(), extEvt);

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
            } else {
                wfRun.processEvent(evt, tasksToSchedule, timersToSchedule);
            }
        } else {
            wfRun.wfSpec = spec;
            wfRun.stores = wsa;
            wfRun.processEvent(evt, tasksToSchedule, timersToSchedule);
        }

        // Schedule tasks
        for (TaskScheduleRequest r : tasksToSchedule) {
            GenericOutput taskOutput = new GenericOutput();
            taskOutput.request = r;
            context.forward(
                new Record<>(key, taskOutput, timestamp),
                ServerTopology.schedulerTaskSink
            );
        }

        for (LHTimer timer : timersToSchedule) {
            GenericOutput out = new GenericOutput();
            out.timer = timer;
            context.forward(
                new Record<>(key, out, timestamp),
                ServerTopology.newTimerSink
            );
        }

        // Forward the observability events
        GenericOutput oeOutput = new GenericOutput();
        oeOutput.observabilityEvents = wfRun.oEvents;
        context.forward(
            new Record<>(key, oeOutput, timestamp),
            ServerTopology.schedulerWfRunSink
        );

        // Update the Data Store and send objects to be Tagged+Indexed
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

        // WfRuns
        wfRunStore.put(key, wfRun);
        forwardforTagging(wfRun, timestamp);
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
        WfSpec out = wfSpecCache.get(id);
        if (out == null) {
            out = wfSpecStore.get(id);
            out.addMetaDependencies(taskDefStore, eedStore);
        }
        return out;
    }
}
