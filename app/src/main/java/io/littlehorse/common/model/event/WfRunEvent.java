package io.littlehorse.common.model.event;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.WfRunEventPb;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class WfRunEvent extends LHSerializable<WfRunEventPb> {

    public String wfRunId;
    public String wfSpecId;
    public Date time;

    public EventCase type;
    public TaskStartedEvent startedEvent;
    public TaskResultEvent taskResult;
    public WfRunRequest runRequest;
    public ExternalEvent externalEvent;

    public Integer threadRunNumber;

    public WfRunEventPb.Builder toProto() {
        WfRunEventPb.Builder b = WfRunEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setTime(LHUtil.fromDate(time));

        if (wfSpecId != null) {
            b.setWfSpecId(wfSpecId);
        }
        if (threadRunNumber != null) {
            b.setThreadRunNumber(threadRunNumber);
        }
        b.clearEvent();

        switch (type) {
            case EVENT_NOT_SET:
                break;
            case STARTED_EVENT:
                b.setStartedEvent(startedEvent.toProto());
                break;
            case TASK_RESULT:
                b.setTaskResult(taskResult.toProto());
                break;
            case RUN_REQUEST:
                b.setRunRequest(runRequest.toProto());
                break;
            case EXTERNAL_EVENT:
                b.setExternalEvent(externalEvent.toProto());
                break;
        }
        return b;
    }

    public Class<WfRunEventPb> getProtoBaseClass() {
        return WfRunEventPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        WfRunEventPb proto = (WfRunEventPb) p;
        this.wfRunId = proto.getWfRunId();
        if (proto.hasWfSpecId()) this.wfSpecId = proto.getWfSpecId();
        this.time = LHUtil.fromProtoTs(proto.getTime());

        if (proto.hasThreadRunNumber()) threadRunNumber = proto.getThreadRunNumber();

        this.type = proto.getEventCase();

        switch (this.type) {
            case EVENT_NOT_SET:
                break;
            case STARTED_EVENT:
                this.startedEvent =
                    TaskStartedEvent.fromProto(proto.getStartedEvent());
                break;
            case TASK_RESULT:
                this.taskResult = TaskResultEvent.fromProto(proto.getTaskResult());
                break;
            case RUN_REQUEST:
                this.runRequest = WfRunRequest.fromProto(proto.getRunRequest());
                break;
            case EXTERNAL_EVENT:
                this.externalEvent =
                    ExternalEvent.fromProto(proto.getExternalEvent());
        }
    }

    public static WfRunEvent fromProto(WfRunEventPb proto) {
        WfRunEvent out = new WfRunEvent();
        out.initFrom(proto);
        return out;
    }

    public WfRunSubEvent getSubEvent() {
        switch (type) {
            case STARTED_EVENT:
                return startedEvent;
            case TASK_RESULT:
                return taskResult;
            case RUN_REQUEST:
                return runRequest;
            case EXTERNAL_EVENT:
                return externalEvent;
            case EVENT_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    public Integer getThreadRunNumber() {
        return getSubEvent().getThreadRunNumber();
    }

    public Integer getNodeRunPosition() {
        return getSubEvent().getNodeRunPosition();
    }
}
