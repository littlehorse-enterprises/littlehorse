package io.littlehorse.jlib.wfsdk.internal;

import io.littlehorse.jlib.wfsdk.SpawnedThread;

public class SpawnedThreadImpl implements SpawnedThread {

    public ThreadBuilderImpl parent;
    public WfRunVariableImpl internalThreadVar;
    public String childThreadName;

    public SpawnedThreadImpl(
        ThreadBuilderImpl parent,
        String childThreadName,
        WfRunVariableImpl internalThreadVar
    ) {
        this.parent = parent;
        this.childThreadName = childThreadName;
        this.internalThreadVar = internalThreadVar;
    }
}
