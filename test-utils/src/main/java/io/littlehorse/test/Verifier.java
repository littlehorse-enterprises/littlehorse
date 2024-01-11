package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.UUID;

public interface Verifier {

    default WfRunId start() {
        String id = UUID.randomUUID().toString().replace("-", "");
        WfRunId wfId = WfRunId.newBuilder().setId(id).build();
        return start(wfId);
    }

    WfRunId start(WfRunId id);
}
