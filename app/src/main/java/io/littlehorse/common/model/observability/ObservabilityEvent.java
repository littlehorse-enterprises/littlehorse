package io.littlehorse.common.model.observability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ObservabilityEventPb;
import io.littlehorse.common.proto.ObservabilityEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class ObservabilityEvent extends LHSerializable<ObservabilityEventPb> {

    public Date time;
    public EventCase type;

    public WfRunStatusChangeOe wfRunStatus;

    public RunStartOe runStart;
    public ThreadStartOe threadStart;
    public TaskScheduledOe taskSchedule;
    public TaskStartOe taskStart;
    public TaskResultOe taskResult;
    public ThreadStatusChangeOe threadStatus;

    public ObservabilityEventPb.Builder toProto() {
        ObservabilityEventPb.Builder out = ObservabilityEventPb
            .newBuilder()
            .setTime(LHUtil.fromDate(time));

        switch (type) {
            case EVENT_NOT_SET:
                break;
            case RUN_START:
                out.setRunStart(runStart.toProto());
                break;
            case THREAD_START:
                out.setThreadStart(threadStart.toProto());
                break;
            case TASK_SCHEDULE:
                out.setTaskSchedule(taskSchedule.toProto());
                break;
            case TASK_START:
                out.setTaskStart(taskStart.toProto());
                break;
            case TASK_RESULT:
                out.setTaskResult(taskResult.toProto());
                break;
            case THREAD_STATUS:
                out.setThreadStatus(threadStatus.toProto());
                break;
            case WF_RUN_STATUS:
                out.setWfRunStatus(wfRunStatus.toProto());
                break;
        }

        return out;
    }

    @JsonIgnore
    public Class<ObservabilityEventPb> getProtoBaseClass() {
        return ObservabilityEventPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ObservabilityEventPb p = (ObservabilityEventPb) proto;
        time = LHUtil.fromProtoTs(p.getTime());
        type = p.getEventCase();
        switch (type) {
            case EVENT_NOT_SET:
                break;
            case RUN_START:
                runStart = RunStartOe.fromProto(p.getRunStart());
                break;
            case THREAD_START:
                threadStart = ThreadStartOe.fromProto(p.getThreadStart());
                break;
            case TASK_SCHEDULE:
                taskSchedule = TaskScheduledOe.fromProto(p.getTaskSchedule());
                break;
            case TASK_START:
                taskStart = TaskStartOe.fromProto(p.getTaskStart());
                break;
            case TASK_RESULT:
                taskResult = TaskResultOe.fromProto(p.getTaskResult());
                break;
            case THREAD_STATUS:
                threadStatus = ThreadStatusChangeOe.fromProto(p.getThreadStatus());
                break;
            case WF_RUN_STATUS:
                wfRunStatus = WfRunStatusChangeOe.fromProto(p.getWfRunStatus());
                break;
        }
    }

    public ObservabilityEvent() {}

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

    public ObservabilityEvent(TaskResultOe evt, Date time) {
        this.time = time;
        type = EventCase.TASK_RESULT;
        taskResult = evt;
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
