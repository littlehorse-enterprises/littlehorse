package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;

public class TaskNodeOutputImpl extends NodeOutputImpl implements TaskNodeOutput {

    public TaskNodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }

    @Override
    public TaskNodeOutputImpl withNoRetries() {
        parent.removeRetryPolicy(this);
        return this;
    }

    @Override
    public TaskNodeOutputImpl withExponentialBackoff(ExponentialBackoffRetryPolicy policy) {
        parent.overrideTaskExponentialBackoffPolicy(this, policy);
        return this;
    }

    @Override
    public TaskNodeOutputImpl withSimpleRetries(int retries) {
        parent.overrideTaskSimpleRetries(this, retries);
        return this;
    }
}
