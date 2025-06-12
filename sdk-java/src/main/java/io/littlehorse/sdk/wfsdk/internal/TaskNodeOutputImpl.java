package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;

public class TaskNodeOutputImpl extends NodeOutputImpl implements TaskNodeOutput {

    public TaskNodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }

    @Override
    public TaskNodeOutputImpl withExponentialBackoff(ExponentialBackoffRetryPolicy policy) {
        parent.overrideTaskExponentialBackoffPolicy(this, policy);
        return this;
    }

    @Override
    public TaskNodeOutputImpl withRetries(int retries) {
        parent.overrideTaskRetries(this, retries);
        return this;
    }

    @Override
    public TaskNodeOutput timeout(int timeoutSeconds) {
        parent.addTimeoutToTaskNode(this, timeoutSeconds);
        return this;
    }
}
