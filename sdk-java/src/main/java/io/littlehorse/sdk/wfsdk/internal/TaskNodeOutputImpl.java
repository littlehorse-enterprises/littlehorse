package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.TaskNodeOutput;

public class TaskNodeOutputImpl extends NodeOutputImpl implements TaskNodeOutput {

    public TaskNodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }

    @Override
    public void withRetries(int retries) {
        parent.overrideTaskRetries(this, retries);
    }
}
