package io.littlehorse.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.WFRunEventPb.EventCase;
import io.littlehorse.scheduler.model.WfRunState;

public class SchedulerProcessor
    implements Processor<String, WFRunEvent, String, SchedulerOutput>
{
    private KeyValueStore<String, WfRunState> wfRunStore;
    private KeyValueStore<String, WfSpec> wfSpecStore;
    private Map<String, WfSpec> wfSpecCache;
    private ProcessorContext<String, SchedulerOutput> context;

    public SchedulerProcessor(LHConfig config) {

    }

    @Override
    public void init(final ProcessorContext<String, SchedulerOutput> context) {
        wfRunStore = context.getStateStore(LHConstants.WF_RUN_STORE_NAME);
        wfSpecStore = context.getStateStore(LHConstants.WF_SPEC_STORE_NAME);
        this.context = context;
        this.wfSpecCache = new HashMap<>();
    }

    @Override
    public void process(final Record<String, WFRunEvent> record) {
        try {
            processHelper(record);
        } catch(Exception exn) {
            String wfRunId = record.key();
            WfRunState wfRun = wfRunStore.get(wfRunId);
            if (wfRun == null) {
                exn.printStackTrace();
                return;
            }
            try {
                exn.printStackTrace();
                wfRun.status = LHStatusPb.ERROR;
                System.out.println(
                    "Bad WFRun, putting in ERROR. TODO: add a FailureMessage to WFRun"
                );
                wfRunStore.put(wfRunId, wfRun);
            } catch (Exception exn2) {
                exn2.printStackTrace();
            }
        }
    }

    private WfSpec getWfSpec(String id) {
        WfSpec out = wfSpecCache.get(id);
        if (out == null) {
            out = wfSpecStore.get(id);
            wfSpecCache.put(id, out);
        }
        return out;
    }

    private void processHelper(final Record<String, WFRunEvent> record) {
        WfSpec spec = getWfSpec(record.value().wfSpecId);
        if (spec == null) {
            System.out.println("Couldn't find spec, TODO: DeadLetter Queue");
            return;
        }

        WFRunEvent e = record.value();
        WfRunState wfRun = wfRunStore.get(record.key());

        List<TaskScheduleRequest> toSchedule = new ArrayList<>();

        if (e.type == EventCase.RUN_REQUEST) {
            if (wfRun != null) {
                System.out.println(
                    "Got a past run for id " + record.key() + ", skipping"
                );
                return;
            }
            wfRun = spec.startNewRun(e, toSchedule);

        } else {
            wfRun.wfSpec = spec;
            wfRun.processEvent(e, toSchedule);
        }

        // Schedule tasks
        for (TaskScheduleRequest r: toSchedule) {
            SchedulerOutput taskOutput = new SchedulerOutput();
            taskOutput.request = r;
            context.forward(new Record<>(
                record.key(), taskOutput, record.timestamp()
            ), SchedulerTopology.taskSchedulerSink);
        }

        // Forward the observability events
        SchedulerOutput oeOutput = new SchedulerOutput();
        oeOutput.observabilityEvents = wfRun.oEvents;
        context.forward(new Record<>(
            record.key(), oeOutput, record.timestamp()
        ), SchedulerTopology.wfRunSink);

        // Save the WfRunState
        wfRunStore.put(record.key(), wfRun);
    }
}
