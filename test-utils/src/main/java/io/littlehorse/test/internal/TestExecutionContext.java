package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.proto.WfRunId;

public class TestExecutionContext {

    private final WfRunId wfRunId;

    public TestExecutionContext(WfRunId id) {
        this.wfRunId = id;
    }

    public WfRunId getWfRunId() {
        return wfRunId;
    }
}
