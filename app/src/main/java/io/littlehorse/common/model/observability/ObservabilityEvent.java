package io.littlehorse.common.model.observability;

import java.util.Date;
import io.littlehorse.common.proto.ObservabilityEventPb;
import io.littlehorse.common.proto.ObservabilityEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;

public class ObservabilityEvent {
    public Date time;
    public EventCase type;

    public WfRunStatusChangeOe wfRunStatus;

    public RunStartOe runStart;
    public ThreadStartOe threadStart;
    public TaskScheduledOe taskSchedule;
    public TaskStartOe taskStart;
    public TaskCompleteOe taskComplete;
    public ThreadStatusChangeOe threadStatus;

    public ObservabilityEventPb.Builder toProtoBuilder() {
        ObservabilityEventPb.Builder out = ObservabilityEventPb.newBuilder()
            .setTime(LHUtil.fromDate(time));

        switch(type) {
        case EVENT_NOT_SET:
            break;

        case RUN_START:
            out.setRunStart(runStart.toProtoBuilder());
            break;

        case THREAD_START:
            out.setThreadStart(threadStart.toProtoBuilder());
            break;

        case TASK_SCHEDULE:
            out.setTaskSchedule(taskSchedule.toProtoBuilder());
            break;

        case TASK_START:
            out.setTaskStart(taskStart.toProtoBuilder());
            break;

        case TASK_COMPLETE:
            out.setTaskComplete(taskComplete.toProtoBuilder());
            break;

        case THREAD_STATUS:
            out.setThreadStatus(threadStatus.toProtoBuilder());
            break;

        case WF_RUN_STATUS:
            out.setWfRunStatus(wfRunStatus.toProtoBuilder());
            break;
        }

        return out;
    }

    public ObservabilityEvent(RunStartOe evt, Date time) {
        type = EventCase.RUN_START;
        this.time = time;
        runStart = evt;
    }

    public ObservabilityEvent(ThreadStartOe evt, Date time) {
        this.time = time;
        type = EventCase.THREAD_START;
        threadStart = evt;
    }

    public ObservabilityEvent(TaskScheduledOe evt, Date time) {
        this.time = time;
        type = EventCase.TASK_SCHEDULE;
        taskSchedule = evt;
    }

    public ObservabilityEvent(TaskStartOe evt, Date time) {
        this.time = time;
        type = EventCase.TASK_START;
        taskStart = evt;
    }

    public ObservabilityEvent(TaskCompleteOe evt, Date time) {
        this.time = time;
        type=  EventCase.TASK_COMPLETE;
        taskComplete = evt;
    }

    public ObservabilityEvent(ThreadStatusChangeOe evt, Date time) {
        this.time = time;
        type = EventCase.THREAD_STATUS;
        threadStatus = evt;
    }

    public ObservabilityEvent(WfRunStatusChangeOe evt, Date time) {
        this.time = time;
        type = EventCase.WF_RUN_STATUS;
        wfRunStatus = evt;
    }
}