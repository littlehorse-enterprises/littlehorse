package io.littlehorse.common.model.observability;

import io.littlehorse.common.proto.RunStartOePb;

public class RunStartOe {
    public String wfSpecId;

    public RunStartOePb.Builder toProtoBuilder() {
        return RunStartOePb.newBuilder().setWfSpecId(wfSpecId);
    }

    public RunStartOe(String wfSpecId) {
        this.wfSpecId = wfSpecId;
    }

    // No need to implement loading from protobuf since this repo only writes.
}
