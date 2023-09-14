package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import lombok.Getter;

@Getter
class SpawnedThreadsImpl implements SpawnedThreads {

    private ThreadBuilderImpl parent;
    private WfRunVariableImpl internalThreadVar;

    SpawnedThreadsImpl(ThreadBuilderImpl parent, WfRunVariableImpl internalThreadVar) {
        this.parent = parent;
        this.internalThreadVar = internalThreadVar;
    }
}
