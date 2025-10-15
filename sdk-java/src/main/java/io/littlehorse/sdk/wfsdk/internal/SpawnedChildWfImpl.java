package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.SpawnedChildWf;

public class SpawnedChildWfImpl implements SpawnedChildWf {

    private String sourceNodeName;
    private WorkflowThreadImpl thread;

    public SpawnedChildWfImpl(String sourceNodeName, WorkflowThreadImpl thread) {
        this.thread = thread;
        this.sourceNodeName = sourceNodeName;
    }

    public String getSourceNodeName() {
        return sourceNodeName;
    }

    public WorkflowThreadImpl getThread() {
        return thread;
    }
}
