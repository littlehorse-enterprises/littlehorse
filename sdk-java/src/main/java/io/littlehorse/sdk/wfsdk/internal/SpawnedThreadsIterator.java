package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;

public class SpawnedThreadsIterator implements SpawnedThreads {

    private final WfRunVariableImpl internalStartedThreadVar;
    public SpawnedThreadsIterator(WfRunVariableImpl internalStartedThreadVar) {
        this.internalStartedThreadVar = internalStartedThreadVar;
    }
    @Override
    public WaitForThreadsNode buildNode() {
        return null;
    }
}
