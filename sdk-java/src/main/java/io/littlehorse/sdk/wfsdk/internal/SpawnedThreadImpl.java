package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.WfRunVariable;

class SpawnedThreadImpl implements SpawnedThread {

    public WorkflowThreadImpl parent;
    public WfRunVariableImpl internalThreadVar;
    public String childThreadName;

    public SpawnedThreadImpl(WorkflowThreadImpl parent, String childThreadName, WfRunVariableImpl internalThreadVar) {
        this.parent = parent;
        this.childThreadName = childThreadName;
        this.internalThreadVar = internalThreadVar;
    }

    @Override
    public WfRunVariable getThreadNumberVariable() {
        return internalThreadVar;
    }
}
