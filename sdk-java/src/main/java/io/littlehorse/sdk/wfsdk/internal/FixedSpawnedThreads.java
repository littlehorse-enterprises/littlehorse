package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import java.util.Collection;
import java.util.List;
import lombok.Getter;

@Getter
public final class FixedSpawnedThreads implements SpawnedThreads {

    private final Collection<SpawnedThread> spawnedThreads;

    public FixedSpawnedThreads(SpawnedThread... spawnedThreads) {
        this.spawnedThreads = List.of(spawnedThreads);
    }

    @Override
    public WaitForThreadsNode buildNode() {
        WaitForThreadsNode.Builder waitNode = WaitForThreadsNode.newBuilder();
        for (SpawnedThread spawnedThread : spawnedThreads) {
            WfRunVariableImpl threadNumberVariable = (WfRunVariableImpl) spawnedThread.getThreadNumberVariable();
            if (!threadNumberVariable.getType().equals(VariableType.INT)) {
                throw new IllegalArgumentException("Only int variables are supported");
            }
            WaitForThreadsNode.ThreadToWaitFor threadToWaitFor = WaitForThreadsNode.ThreadToWaitFor.newBuilder()
                    .setThreadRunNumber(BuilderUtil.assignVariable(threadNumberVariable))
                    .build();
            waitNode.addThreads(threadToWaitFor);
        }
        waitNode.setPolicy(WaitForThreadsPolicy.STOP_ON_FAILURE);
        return waitNode.build();
    }
}
