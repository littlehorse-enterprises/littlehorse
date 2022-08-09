package io.littlehorse.common.model.event;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.scheduler.WfRunRequestPb;
import io.littlehorse.common.proto.scheduler.WfRunRequestPbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class WfRunRequest extends LHSerializable<WfRunRequestPb> {
    public String wfRunId;
    public String wfSpecId;

    public WfRunRequestPb.Builder toProto() {
        if (wfRunId == null) {
            wfRunId = LHUtil.generateGuid();
        }
        WfRunRequestPb.Builder b = WfRunRequestPb.newBuilder()
            .setWfRunId(wfRunId)
            .setWfSpecId(wfSpecId);

        return b;
    }

    public void initFrom(MessageOrBuilder p) {
        WfRunRequestPbOrBuilder proto = (WfRunRequestPbOrBuilder) p;
        if (proto.hasWfRunId()) {
            wfRunId = proto.getWfRunId();
        } else {
            wfRunId = LHUtil.generateGuid();
        }
        wfSpecId = proto.getWfSpecId();
    }

    public Class<WfRunRequestPb> getProtoBaseClass() {
        return WfRunRequestPb.class;
    }

    public static WfRunRequest fromProto(WfRunRequestPbOrBuilder proto) {
        WfRunRequest out = new WfRunRequest();
        out.initFrom(proto);
        return out;
    }
}
