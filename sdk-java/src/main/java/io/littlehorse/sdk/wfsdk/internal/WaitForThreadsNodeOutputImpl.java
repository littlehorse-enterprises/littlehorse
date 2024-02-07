package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;

class WaitForThreadsNodeOutputImpl extends NodeOutputImpl implements WaitForThreadsNodeOutput {

    private final ThreadSpec.Builder threadSpec;

    public WaitForThreadsNodeOutputImpl(String nodeName, WorkflowThreadImpl parent, ThreadSpec.Builder threadSpec) {
        super(nodeName, parent);
        this.threadSpec = threadSpec;
    }
}
