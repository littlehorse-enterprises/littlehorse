package io.littlehorse.common.model.observabilityevent;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.observabilityevent.events.ExtEvtMatchedOe;
import io.littlehorse.common.model.observabilityevent.events.ExtEvtRegisteredOe;
import io.littlehorse.common.model.observabilityevent.events.InterruptedOe;
import io.littlehorse.common.model.observabilityevent.events.TaskResultOe;
import io.littlehorse.common.model.observabilityevent.events.TaskScheduledOe;
import io.littlehorse.common.model.observabilityevent.events.TaskStartOe;
import io.littlehorse.common.model.observabilityevent.events.ThreadStartOe;
import io.littlehorse.common.model.observabilityevent.events.ThreadStatusOe;
import io.littlehorse.common.model.observabilityevent.events.WaitingForExtEvtOe;
import io.littlehorse.common.model.observabilityevent.events.WfRunStartOe;
import io.littlehorse.common.model.observabilityevent.events.WfRunStatusOe;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.proto.ObservabilityEventPb;
import io.littlehorse.jlib.common.proto.ObservabilityEventPb.EventCase;
import io.littlehorse.jlib.common.proto.ObservabilityEventPbOrBuilder;
import java.util.Date;

public class ObservabilityEvent extends LHSerializable<ObservabilityEventPb> {

    public String wfRunId;
    public Date time;
    public EventCase type;

    public WfRunStartOe wfRunStart;
    public ThreadStartOe threadStart;
    public TaskScheduledOe taskScheduled;
    public TaskStartOe taskStarted;
    public TaskResultOe taskResult;
    public ExtEvtRegisteredOe extEvtRegistered;
    public WaitingForExtEvtOe waitingForExtEvt;
    public ExtEvtMatchedOe extEvtMatched;
    public InterruptedOe interrupted;
    public ThreadStatusOe threadStatus;
    public WfRunStatusOe wfRunStatus;

    public Class<ObservabilityEventPb> getProtoBaseClass() {
        return ObservabilityEventPb.class;
    }

    public ObservabilityEventPb.Builder toProto() {
        ObservabilityEventPb.Builder out = ObservabilityEventPb.newBuilder();
        out.setWfRunId(wfRunId).setTime(LHLibUtil.fromDate(time));

        switch (type) {
            case WF_RUN_START:
                out.setWfRunStart(wfRunStart.toProto());
                break;
            case THREAD_START:
                out.setThreadStart(threadStart.toProto());
                break;
            case TASK_START:
                out.setTaskStart(taskStarted.toProto());
                break;
            case TASK_RESULT:
                out.setTaskResult(taskResult.toProto());
                break;
            case EXT_EVT_MATCHED:
                out.setExtEvtMatched(extEvtMatched.toProto());
                break;
            case EXT_EVT_REGISTERED:
                out.setExtEvtRegistered(extEvtRegistered.toProto());
                break;
            case WAITING_FOR_EXT_EVT:
                out.setWaitingForExtEvt(waitingForExtEvt.toProto());
                break;
            case INTERRUPTED:
                out.setInterrupted(interrupted.toProto());
                break;
            case THREAD_STATUS:
                out.setThreadStatus(threadStatus.toProto());
                break;
            case WF_RUN_STATUS:
                out.setWfRunStatus(wfRunStatus.toProto());
                break;
            case EVENT_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        ObservabilityEventPbOrBuilder p = (ObservabilityEventPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        type = p.getEventCase();

        switch (type) {
            case WF_RUN_START:
                wfRunStart =
                    LHSerializable.fromProto(p.getWfRunStart(), WfRunStartOe.class);
                break;
            case THREAD_START:
                threadStart =
                    LHSerializable.fromProto(p.getThreadStart(), ThreadStartOe.class);
                break;
            case TASK_START:
                taskStarted =
                    LHSerializable.fromProto(p.getTaskStart(), TaskStartOe.class);
                break;
            case TASK_RESULT:
                taskResult =
                    LHSerializable.fromProto(p.getTaskResult(), TaskResultOe.class);
                break;
            case EXT_EVT_MATCHED:
                extEvtMatched =
                    LHSerializable.fromProto(
                        p.getExtEvtMatched(),
                        ExtEvtMatchedOe.class
                    );
                break;
            case EXT_EVT_REGISTERED:
                extEvtRegistered =
                    LHSerializable.fromProto(
                        p.getExtEvtRegistered(),
                        ExtEvtRegisteredOe.class
                    );
                break;
            case WAITING_FOR_EXT_EVT:
                waitingForExtEvt =
                    LHSerializable.fromProto(
                        p.getWaitingForExtEvt(),
                        WaitingForExtEvtOe.class
                    );
                break;
            case INTERRUPTED:
                interrupted =
                    LHSerializable.fromProto(p.getInterrupted(), InterruptedOe.class);
                break;
            case THREAD_STATUS:
                threadStatus =
                    LHSerializable.fromProto(
                        p.getThreadStatus(),
                        ThreadStatusOe.class
                    );
                break;
            case WF_RUN_STATUS:
                wfRunStatus =
                    LHSerializable.fromProto(p.getWfRunStatus(), WfRunStatusOe.class);
                break;
            case EVENT_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    public SubEvent<?> getSubEvent() {
        switch (type) {
            case WF_RUN_START:
                return wfRunStart;
            case THREAD_START:
                return threadStart;
            case TASK_SCHEDULED:
                return taskScheduled;
            case TASK_START:
                return taskStarted;
            case TASK_RESULT:
                return taskResult;
            case EXT_EVT_MATCHED:
                return extEvtMatched;
            case EXT_EVT_REGISTERED:
                return extEvtRegistered;
            case WAITING_FOR_EXT_EVT:
                return waitingForExtEvt;
            case INTERRUPTED:
                return interrupted;
            case THREAD_STATUS:
                return threadStatus;
            case WF_RUN_STATUS:
                return wfRunStatus;
            case EVENT_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    public void setSubEvent(SubEvent<?> evt) {
        if (WfRunStartOe.class.equals(evt.getClass())) {
            wfRunStart = (WfRunStartOe) evt;
            type = EventCase.WF_RUN_START;
        } else if (ThreadStartOe.class.equals(evt.getClass())) {
            threadStart = (ThreadStartOe) evt;
            type = EventCase.THREAD_START;
        } else if (TaskScheduledOe.class.equals(evt.getClass())) {
            taskScheduled = (TaskScheduledOe) evt;
            type = EventCase.TASK_SCHEDULED;
        } else if (TaskStartOe.class.equals(evt.getClass())) {
            taskStarted = (TaskStartOe) evt;
            type = EventCase.TASK_START;
        } else if (TaskResultOe.class.equals(evt.getClass())) {
            taskResult = (TaskResultOe) evt;
            type = EventCase.TASK_RESULT;
        } else if (ExtEvtRegisteredOe.class.equals(evt.getClass())) {
            extEvtRegistered = (ExtEvtRegisteredOe) evt;
            type = EventCase.EXT_EVT_REGISTERED;
        } else if (ExtEvtMatchedOe.class.equals(evt.getClass())) {
            extEvtMatched = (ExtEvtMatchedOe) evt;
            type = EventCase.EXT_EVT_MATCHED;
        } else if (WaitingForExtEvtOe.class.equals(evt.getClass())) {
            waitingForExtEvt = (WaitingForExtEvtOe) evt;
            type = EventCase.WAITING_FOR_EXT_EVT;
        } else if (InterruptedOe.class.equals(evt.getClass())) {
            interrupted = (InterruptedOe) evt;
            type = EventCase.INTERRUPTED;
        } else if (ThreadStatusOe.class.equals(evt.getClass())) {
            threadStatus = (ThreadStatusOe) evt;
            type = EventCase.THREAD_STATUS;
        } else if (WfRunStatusOe.class.equals(evt.getClass())) {
            wfRunStatus = (WfRunStatusOe) evt;
            type = EventCase.WF_RUN_STATUS;
        } else {
            throw new RuntimeException("not possible");
        }
    }
}
