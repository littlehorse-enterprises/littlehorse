package io.littlehorse.common.model.event;

import io.littlehorse.common.proto.scheduler.WFRunRequestPb;
import io.littlehorse.common.proto.scheduler.WFRunRequestPbOrBuilder;

public class WFRunRequest {
    public String wfRunId;
    public String wfSpecId;

    public WFRunRequestPb.Builder toProtoBuilder() {
        WFRunRequestPb.Builder b = WFRunRequestPb.newBuilder()
            .setWfRunId(wfRunId)
            .setWfSpecId(wfSpecId);

        return b;
    }

    public static WFRunRequest fromProto(WFRunRequestPbOrBuilder proto) {
        WFRunRequest out = new WFRunRequest();
        out.wfRunId = proto.getWfRunId();
        out.wfSpecId = proto.getWfSpecId();
        return out;
    }
}
