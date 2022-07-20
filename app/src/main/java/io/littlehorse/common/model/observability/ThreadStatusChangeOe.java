package io.littlehorse.common.model.observability;

import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadStatusChangeOePb;

public class ThreadStatusChangeOe {
    public int threadRunNumber;
    public LHStatusPb status;

    public ThreadStatusChangeOePb.Builder toProtoBuilder() {
        ThreadStatusChangeOePb.Builder out = ThreadStatusChangeOePb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setStatus(status);

        return out;
    }
}
