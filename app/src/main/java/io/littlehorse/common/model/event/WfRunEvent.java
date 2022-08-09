package io.littlehorse.common.model.event;

import java.util.Date;
import io.littlehorse.common.proto.scheduler.WfRunEventPb;
import io.littlehorse.common.proto.scheduler.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;

public class WfRunEvent {
    public String wfRunId;
    public String wfSpecId;
    public Date time;

    public EventCase type;
    public TaskStartedEvent startedEvent;
    public TaskCompletedEvent completedEvent;
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
        case COMPLETED_EVENT:
            b.setCompletedEvent(completedEvent.toProto());
            break;
        case RUN_REQUEST:
            b.setRunRequest(runRequest.toProto());
            break;
        }
        return b;
    }

    public static WfRunEvent fromProto(WfRunEventPb proto) {
        WfRunEvent out = new WfRunEvent();
        out.wfRunId = proto.getWfRunId();
        out.wfSpecId = proto.getWfSpecId();
        out.time = LHUtil.fromProtoTs(proto.getTime());

        out.type = proto.getEventCase();

        switch (out.type) {
        case EVENT_NOT_SET:
            break;
        case STARTED_EVENT:
            out.startedEvent = TaskStartedEvent.fromProto(proto.getStartedEvent());
            break;
        case COMPLETED_EVENT:
            out.completedEvent = TaskCompletedEvent
                .fromProto(proto.getCompletedEvent());
            break;
        case RUN_REQUEST:
            out.runRequest = WfRunRequest.fromProto(proto.getRunRequest());
        }
        return out;
    }
}
