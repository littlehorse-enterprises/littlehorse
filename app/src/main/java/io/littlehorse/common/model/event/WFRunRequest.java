package io.littlehorse.common.model.event;

import io.littlehorse.common.proto.scheduler.WfRunRequestPb;
import io.littlehorse.common.proto.scheduler.WfRunRequestPbOrBuilder;

public class WFRunRequest {
    public String wfRunId;
    public String wfSpecId;

    public WfRunRequestPb.Builder toProtoBuilder() {
        WfRunRequestPb.Builder b = WfRunRequestPb.newBuilder()
            .setWfRunId(wfRunId)
            .setWfSpecId(wfSpecId);

        return b;
    }

    public static WFRunRequest fromProto(WfRunRequestPbOrBuilder proto) {
        WFRunRequest out = new WFRunRequest();
        out.wfRunId = proto.getWfRunId();
        out.wfSpecId = proto.getWfSpecId();
        return out;
    }
}
