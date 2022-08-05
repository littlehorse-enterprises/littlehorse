package io.littlehorse.common.model.event;

import java.util.Date;
import io.littlehorse.common.proto.scheduler.WFRunEventPb;
import io.littlehorse.common.proto.scheduler.WFRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;

public class WFRunEvent {
    public String wfRunId;
    public String wfSpecId;
    public Date time;

    public EventCase type;
    public TaskStartedEvent startedEvent;
    public TaskCompletedEvent completedEvent;
    public WFRunRequest runRequest;

    public WFRunEventPb.Builder toProtoBuilder() {
        WFRunEventPb.Builder b = WFRunEventPb.newBuilder()
            .setWfRunId(wfRunId)
            .setWfSpecId(wfSpecId)
            .setTime(LHUtil.fromDate(time));

        b.clearEvent();

        switch (type) {
        case EVENT_NOT_SET:
            break;
        case STARTED_EVENT:
            b.setStartedEvent(startedEvent.toProtoBuilder());
            break;
        case COMPLETED_EVENT:
            b.setCompletedEvent(completedEvent.toProtoBuilder());
            break;
        case RUN_REQUEST:
            b.setRunRequest(runRequest.toProtoBuilder());
            break;
        }
        return b;
    }

    public static WFRunEvent fromProto(WFRunEventPb proto) {
        WFRunEvent out = new WFRunEvent();
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
            out.runRequest = WFRunRequest.fromProto(proto.getRunRequest());
        }
        return out;
    }
}
