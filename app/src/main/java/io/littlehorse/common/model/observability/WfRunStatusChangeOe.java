package io.littlehorse.common.model.observability;

import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.WfRunStatusChangeOePb;

public class WfRunStatusChangeOe {
    public LHStatusPb status;

    public WfRunStatusChangeOePb.Builder toProtoBuilder() {
        WfRunStatusChangeOePb.Builder out = WfRunStatusChangeOePb.newBuilder()
            .setStatus(status);

        return out;
    }
}
