package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsStrategy;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;

public class SpawnedThreadsIterator implements SpawnedThreads {

    private final WfRunVariableImpl internalStartedThreadVar;

    public SpawnedThreadsIterator(final WfRunVariableImpl internalStartedThreadVar) {
        this.internalStartedThreadVar = internalStartedThreadVar;
        if (internalStartedThreadVar.getTypeDef().getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE
                || internalStartedThreadVar.getTypeDef().getPrimitiveType() != VariableType.JSON_ARR) {
            throw new IllegalArgumentException("Only support for json arrays");
        }
    }

    @Override
    public WaitForThreadsNode buildNode(WaitForThreadsStrategy strategy) {
        WaitForThreadsNode.Builder waitNode = WaitForThreadsNode.newBuilder();
        waitNode.setThreadList(BuilderUtil.assignVariable(internalStartedThreadVar));
        waitNode.setStrategy(strategy);
        return waitNode.build();
    }
}
