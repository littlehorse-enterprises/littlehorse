package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.WaitForEventNodeOutput;

public class WaitForEventNodeOutputImpl extends NodeOutputImpl implements WaitForEventNodeOutput {

    public WaitForEventNodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }

    @Override
    public WaitForEventNodeOutput timeout(int timeoutSeconds) {
        parent.addTimeoutToExtEvtNode(this, timeoutSeconds);
        return this;
    }
}
