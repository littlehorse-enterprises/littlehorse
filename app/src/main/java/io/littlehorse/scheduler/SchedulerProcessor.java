package io.littlehorse.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.scheduler.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.scheduler.model.SchedulerTimer;
import io.littlehorse.scheduler.model.WfRunState;

public class SchedulerProcessor
    implements Processor<String, WfRunEvent, String, SchedulerOutput>
{
    private KeyValueStore<String, WfRunState> wfRunStore;
    private KeyValueStore<String, SchedulerTimer> timerStore;
    private Map<String, WfSpec> wfSpecCache;
    private ProcessorContext<String, SchedulerOutput> context;
    private LHDatabaseClient client;
    private Cancellable punctuator;

    public SchedulerProcessor(LHConfig config) {
        this.client = config.getDbClient();
    }

    @Override
    public void init(final ProcessorContext<String, SchedulerOutput> context) {
        wfRunStore = context.getStateStore(LHConstants.SCHED_WF_RUN_STORE_NAME);
        timerStore = context.getStateStore(LHConstants.SCHED_TIMER_STORE_NAME);
        this.context = context;
        this.wfSpecCache = new HashMap<>();

        punctuator = context.schedule(
            LHConstants.PUNCTUATOR_INERVAL,
            PunctuationType.WALL_CLOCK_TIME,
            this::clearTimers
        );
    }

    @Override
    public void process(final Record<String, WfRunEvent> record) {
        safeProcess(record.key(), record.timestamp(), record.value());
    }

    @Override
    public void close() {
        punctuator.cancel();
    }

    private void safeProcess(String key, long timestamp, WfRunEvent value) {
        try {
            processHelper(key, timestamp, value);
        } catch(Exception exn) {
            String wfRunId = key;
            WfRunState wfRun = wfRunStore.get(wfRunId);
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

    private void processHelper(String key, long timestamp, WfRunEvent e) {
        WfSpec spec = getWfSpec(e.wfSpecId);
        if (spec == null) {
            LHUtil.log("Couldn't find spec, TODO: DeadLetter Queue");
            return;
        }

        WfRunState wfRun = wfRunStore.get(key);

        List<TaskScheduleRequest> tasksToSchedule = new ArrayList<>();
        List<SchedulerTimer> timersToSchedule = new ArrayList<>();
        List<String> timersToClear = new ArrayList<>();

        if (e.type == EventCase.RUN_REQUEST) {
            if (wfRun != null) {
                LHUtil.log("Got a past run for id " + key + ", skipping");
                return;
            }
            wfRun = spec.startNewRun(e, tasksToSchedule, timersToSchedule, timersToClear);

        } else {
            wfRun.wfSpec = spec;
            wfRun.processEvent(e, tasksToSchedule, timersToSchedule, timersToClear);
        }

        // Schedule tasks
        for (TaskScheduleRequest r: tasksToSchedule) {
            SchedulerOutput taskOutput = new SchedulerOutput();
            taskOutput.request = r;
            context.forward(new Record<>(
                key, taskOutput, timestamp
            ), Scheduler.taskSchedulerSink);
        }

        for (SchedulerTimer timer: timersToSchedule) {
            String timerStoreKey = timer.getStoreKey();
            timerStore.put(timerStoreKey, timer);
        }

        for (String timerKey: timersToClear) {
            timerStore.delete(timerKey);
        }

        // Forward the observability events
        SchedulerOutput oeOutput = new SchedulerOutput();
        oeOutput.observabilityEvents = wfRun.oEvents;
        context.forward(new Record<>(key, oeOutput, timestamp), Scheduler.wfRunSink);

        // Save the WfRunState
        wfRunStore.put(key, wfRun);
    }

    public void clearTimers(long timestamp) {
        // First timer
        String start = "00000000";
        String end = LHUtil.toLhDbFormat(new Date(timestamp));
        try (KeyValueIterator<String, SchedulerTimer> iter = timerStore.range(start, end)) {
            while (iter.hasNext()) {
                KeyValue<String, SchedulerTimer> entry = iter.next();
                SchedulerTimer timer = entry.value;
                safeProcess(timer.wfRunId, timestamp, timer.event);
                timerStore.delete(entry.key);
            }
        }
    }

    private WfSpec getWfSpec(String id) {
        WfSpec out = wfSpecCache.get(id);
        if (out == null) {
            try {
                out = client.getWfSpec(id);
                System.out.println(out.toJson());
                wfSpecCache.put(id, out);
            } catch(LHConnectionError exn) {
                exn.printStackTrace();
            }
        }
        return out;
    }
}
