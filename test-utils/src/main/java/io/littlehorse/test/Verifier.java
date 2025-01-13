package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.UUID;

public interface Verifier {

    default WfRunId start() {
        String id = UUID.randomUUID().toString().replace("-", "");
        WfRunId wfId = WfRunId.newBuilder().setId(id).build();
        return start(wfId);
    }

    /**
     * Start running all the steps in the verification chain
     * @param id specifies the id for the WfRun. Littlehorse server will create a random ID
     * @return {@link WfRunId} returned by LH Server
     */
    WfRunId start(WfRunId id);
}
