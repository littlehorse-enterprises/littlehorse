package io.littlehorse.server.processors;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.TaskRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
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
    private KeyValueStore<String, TaskRun> taskRunStore;
    private KeyValueStore<String, Variable> variableStore;

    public SchedulerProcessor(LHConfig config) {}

    @Override
    public void init(final ProcessorContext<String, GenericOutput> context) {
        wfRunStore =
            context.getStateStore(GETable.getBaseStoreName(WfRun.class));
        wfSpecStore =
            context.getStateStore(
                GlobalPOSTable.getGlobalStoreName(WfSpec.class)
            );
        taskRunStore =
            context.getStateStore(GETable.getBaseStoreName(TaskRun.class));
        variableStore =
            context.getStateStore(GETable.getBaseStoreName(Variable.class));
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
            taskRunStore,
            variableStore,
            key
        );

        if (evt.type == EventCase.RUN_REQUEST) {
            if (wfRun != null) {
                LHUtil.log("Got a past run for id " + key + ", skipping");
                return;
            }
            wfRun =
                spec.startNewRun(evt, tasksToSchedule, timersToSchedule, wsa);
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

        for (Map.Entry<String, Variable> entry : wsa.variablePuts.entrySet()) {
            variableStore.put(entry.getKey(), entry.getValue());

            // now forward the thing to be indexed.
            GenericOutput varOutput = new GenericOutput();
            varOutput.thingToTag = entry.getValue();
            context.forward(
                new Record<>(key, varOutput, timestamp),
                GETable.getTaggingProcessorName(Variable.class)
            );
        }

        for (Map.Entry<String, TaskRun> entry : wsa.taskPuts.entrySet()) {
            taskRunStore.put(entry.getKey(), entry.getValue());

            // now forward the thing to be indexed.
            GenericOutput varOutput = new GenericOutput();
            varOutput.thingToTag = entry.getValue();
            context.forward(
                new Record<>(key, varOutput, timestamp),
                GETable.getTaggingProcessorName(TaskRun.class)
            );
        }

        // Now forward the WfRun to be indexed.

        // Save the WfRun
        wfRunStore.put(key, wfRun);
    }

    private WfSpec getWfSpec(String id) {
        WfSpec out = wfSpecCache.get(id);
        if (out == null) {
            out = wfSpecStore.get(id);
        }
        return out;
    }
}
