package io.littlehorse.server.processors;

import java.util.HashMap;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.common.model.observability.RunStartOe;
import io.littlehorse.common.model.observability.TaskCompleteOe;
import io.littlehorse.common.model.observability.TaskScheduledOe;
import io.littlehorse.common.model.observability.TaskStartOe;
import io.littlehorse.common.model.observability.ThreadStartOe;
import io.littlehorse.common.model.observability.ThreadStatusChangeOe;
import io.littlehorse.common.model.observability.WfRunStatusChangeOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.wfrun.TaskRun;
import io.littlehorse.server.model.wfrun.ThreadRun;
import io.littlehorse.server.model.wfrun.WfRun;

public class WfRunProcessor implements Processor<
    String, ObservabilityEvents, Void, Void
> {
    // private ProcessorContext<Void, Void> ctx;
    private KeyValueStore<String, WfRun> wfRunStore;
    private KeyValueStore<String, TaskRun> taskRunStore;
    private HashMap<String, WfSpec> wfSpecCache;
    private LHDatabaseClient client;

    public WfRunProcessor(LHConfig config) {
        wfSpecCache = new HashMap<>();
        this.client = config.getDbClient();
    }

    public WfSpec getWfSpec(String id) {
        WfSpec out = wfSpecCache.get(id);
        if (out == null) {
            try {
                out = client.getWfSpec(id);
            } catch(LHConnectionError exn) {
                LHUtil.log(
                    "Caught and suppressed LHConnection error from client: "
                    + exn.getMessage()
                );
                return null;
            }
            wfSpecCache.put(id, out);
        }
        return out;
    }

    @Override public void init(final ProcessorContext<Void, Void> ctx) {
        // this.ctx = ctx;
        this.wfRunStore = ctx.getStateStore(GETable.getBaseStoreName(WfRun.class));
        this.taskRunStore = ctx.getStateStore(
            GETable.getBaseStoreName(TaskRun.class)
        );
    }

    public void process(final Record<String, ObservabilityEvents> record) {
        String wfRunId = record.key();
        ObservabilityEvents events = record.value();

        WfRun wfRun = wfRunStore.get(wfRunId);

        for (ObservabilityEvent evt: events.events) {
            switch(evt.type) {
            case RUN_START:
                wfRun = handleRunStart(wfRunId, evt);
                break;

            case THREAD_START:
                handleThreadStart(wfRun, evt);
                break;

            case TASK_SCHEDULE:
                handleTaskSchedule(wfRun, evt);
                break;

            case TASK_START:
                handleTaskStart(wfRun, evt);
                break;

            case TASK_COMPLETE:
                handleTaskComplete(wfRun, evt);
                break;

            case THREAD_STATUS:
                handleThreadStatus(wfRun, evt);
                break;

            case WF_RUN_STATUS:
                handleWfRunStatus(wfRun, evt);
                break;

            case EVENT_NOT_SET:
                // Nothing to do
            }
        }
        wfRunStore.put(wfRunId, wfRun);
    }

    private WfRun handleRunStart(String wfRunId, ObservabilityEvent evt) {
        RunStartOe rs = evt.runStart;
        WfRun wfRun = new WfRun();
        wfRun.id = wfRunId;
        wfRun.status = LHStatusPb.RUNNING;
        wfRun.wfSpecId = rs.wfSpecId;
        wfRun.wfSpecName = rs.wfSpecName;
        wfRun.startTime = evt.time;

        return wfRun;
    }

    private void handleThreadStart(WfRun wfRun, ObservabilityEvent evt) {
        ThreadStartOe ts = evt.threadStart;
        ThreadRun thr = new ThreadRun();
        thr.wfRunId = wfRun.id;
        thr.number = wfRun.threadRuns.size();
        thr.status = LHStatusPb.RUNNING;
        thr.threadSpecName = ts.threadSpecName;
        thr.numSteps = 0;
        thr.startTime = evt.time;

        wfRun.threadRuns.add(thr);
    }

    public void handleTaskSchedule(WfRun wfRun, ObservabilityEvent evt) {
        TaskScheduledOe ts = evt.taskSchedule;

        TaskRun task = new TaskRun();
        task.wfRunId = wfRun.id;
        task.threadRunNumber = ts.threadRunNumber;
        task.position = ts.taskRunPosition;

        task.number = ts.taskRunNumber;
        task.attemptNumber = ts.taskRunAttemptNumber;
        task.status = LHStatusPb.STARTING;

        task.scheduleTime = evt.time;

        task.wfSpecId = wfRun.wfSpecId;
        task.wfSpecName = wfRun.wfSpecName;
        task.nodeName = ts.nodeName;

        ThreadRun thread = wfRun.threadRuns.get(task.threadRunNumber);
        thread.numSteps++;
        task.threadSpecName = thread.threadSpecName;

        WfSpec wfSpec = getWfSpec(wfRun.wfSpecId);
        if (wfSpec != null) {
            Node node = wfSpec.threadSpecs.get(
                task.threadSpecName
            ).nodes.get(task.nodeName);
            task.taskDefId = node.taskDefName;
        }

        taskRunStore.put(task.getObjectId(), task);
    }

    private void handleTaskStart(WfRun wfRun, ObservabilityEvent evt) {
        TaskStartOe ts = evt.taskStart;
        TaskRun task = taskRunStore.get(
            TaskRun.getStoreKey(wfRun.id, ts.threadRunNumber, ts.taskRunPosition)
        );

        if (task == null) {
            LHUtil.log("tried to start task which hadn't been created yet ):");
            task = new TaskRun();
            task.wfRunId = wfRun.id;
            task.threadRunNumber = ts.threadRunNumber;
            task.position = ts.taskRunPosition;
        }

        task.scheduleTime = evt.time;
        taskRunStore.put(task.getObjectId(), task);
    }

    private void handleTaskComplete(WfRun wfRun, ObservabilityEvent evt) {
        TaskCompleteOe tc = evt.taskComplete;
        TaskRun task = taskRunStore.get(
            TaskRun.getStoreKey(wfRun.id, tc.threadRunNumber, tc.taskRunPosition)
        );

        task.endTime = evt.time;
        task.output = tc.output;
        task.logOutput = tc.logOutput;
        task.status = tc.success ? LHStatusPb.COMPLETED : LHStatusPb.ERROR;

        taskRunStore.put(task.getObjectId(), task);
    }

    private void handleThreadStatus(WfRun wfRun, ObservabilityEvent evt) {
        ThreadStatusChangeOe tsc = evt.threadStatus;
        ThreadRun thread = wfRun.threadRuns.get(tsc.threadRunNumber);
        thread.status = tsc.status;
        if (thread.status == LHStatusPb.COMPLETED 
            || thread.status == LHStatusPb.ERROR
        ) {
            thread.endTime = evt.time;
        }
    }

    private void handleWfRunStatus(WfRun wfRun, ObservabilityEvent evt) {
        WfRunStatusChangeOe wsc = evt.wfRunStatus;
        wfRun.status = wsc.status;

        if (wfRun.status == LHStatusPb.COMPLETED 
            || wfRun.status == LHStatusPb.ERROR
        ) {
            wfRun.endTime = evt.time;
        }
    }
}
