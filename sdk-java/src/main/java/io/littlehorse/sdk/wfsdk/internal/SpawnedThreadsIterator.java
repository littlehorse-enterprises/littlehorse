package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;

public class SpawnedThreadsIterator implements SpawnedThreads {

    private final WfRunVariableImpl internalStartedThreadVar;

    public SpawnedThreadsIterator(final WfRunVariableImpl internalStartedThreadVar) {
        this.internalStartedThreadVar = internalStartedThreadVar;
        if (!internalStartedThreadVar.getTypeDef().getPrimitiveType().equals(VariableType.JSON_ARR)) {
            throw new IllegalArgumentException("Only support for json arrays");
        }
    }

    @Override
    public WaitForThreadsNode buildNode() {
        WaitForThreadsNode.Builder waitNode = WaitForThreadsNode.newBuilder();
        waitNode.setThreadList(BuilderUtil.assignVariable(internalStartedThreadVar));
        return waitNode.build();
    }
}
