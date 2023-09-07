package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import lombok.Getter;

@Getter
class SpawnedThreadsImpl implements SpawnedThreads {

    private ThreadBuilderImpl parent;
    private WfRunVariableImpl internalThreadVar;
    private String childThreadName;

    SpawnedThreadsImpl(ThreadBuilderImpl parent, String childThreadName, WfRunVariableImpl internalThreadVar) {
        this.parent = parent;
        this.internalThreadVar = internalThreadVar;
        this.childThreadName = childThreadName;
    }
}
