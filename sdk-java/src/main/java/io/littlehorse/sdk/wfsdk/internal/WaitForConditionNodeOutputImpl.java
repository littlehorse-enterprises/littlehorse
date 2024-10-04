package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.WaitForConditionNodeOutput;

public class WaitForConditionNodeOutputImpl extends NodeOutputImpl implements WaitForConditionNodeOutput {

    public WaitForConditionNodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }
}
