package io.littlehorse.common.model.event;

import java.util.Date;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.scheduler.WfRunEventPb;
import io.littlehorse.common.proto.scheduler.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;

public class WfRunEvent extends LHSerializable<WfRunEventPb> {
    public String wfRunId;
    public String wfSpecId;
    public Date time;

    public EventCase type;
    public TaskStartedEvent startedEvent;
    public TaskResultEvent taskResult;
    public WfRunRequest runRequest;

    public WfRunEventPb.Builder toProto() {
        WfRunEventPb.Builder b = WfRunEventPb.newBuilder()
            .setWfRunId(wfRunId)
            .setWfSpecId(wfSpecId)
            .setTime(LHUtil.fromDate(time));

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
        }
        return b;
    }

    public Class<WfRunEventPb> getProtoBaseClass() {
        return WfRunEventPb.class;
    }

    public void initFrom(MessageOrBuilder p) {
        WfRunEventPb proto = (WfRunEventPb) p;
        this.wfRunId = proto.getWfRunId();
        this.wfSpecId = proto.getWfSpecId();
        this.time = LHUtil.fromProtoTs(proto.getTime());

        this.type = proto.getEventCase();

        switch (this.type) {
        case EVENT_NOT_SET:
            break;
        case STARTED_EVENT:
            this.startedEvent = TaskStartedEvent.fromProto(proto.getStartedEvent());
            break;
        case TASK_RESULT:
            this.taskResult = TaskResultEvent.fromProto(proto.getTaskResult());
            break;
        case RUN_REQUEST:
            this.runRequest = WfRunRequest.fromProto(proto.getRunRequest());
            break;
        }
    }
    public static WfRunEvent fromProto(WfRunEventPb proto) {
        WfRunEvent out = new WfRunEvent();
        out.initFrom(proto);
        return out;
    }
}
