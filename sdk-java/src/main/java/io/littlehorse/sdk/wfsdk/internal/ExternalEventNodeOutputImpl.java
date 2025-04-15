package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.ExternalEventNodeOutput;

public class ExternalEventNodeOutputImpl extends NodeOutputImpl implements ExternalEventNodeOutput {

    public ExternalEventNodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }

    @Override
    public ExternalEventNodeOutput timeout(int timeoutSeconds) {
        parent.addTimeoutToExtEvtNode(this, timeoutSeconds);
        return this;
    }
}
