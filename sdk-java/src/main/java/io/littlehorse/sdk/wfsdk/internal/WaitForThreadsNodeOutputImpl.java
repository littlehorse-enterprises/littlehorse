package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;

class WaitForThreadsNodeOutputImpl extends NodeOutputImpl implements WaitForThreadsNodeOutput {

    private final ThreadSpec.Builder threadSpec;

    public WaitForThreadsNodeOutputImpl(String nodeName, ThreadBuilderImpl parent, ThreadSpec.Builder threadSpec) {
        super(nodeName, parent);
        this.threadSpec = threadSpec;
    }

    public WaitForThreadsNodeOutputImpl withPolicy(WaitForThreadsPolicy failureStrategy) {
        Node nodesOrThrow = threadSpec.getNodesOrThrow(nodeName);
        WaitForThreadsNode waitForThreads = nodesOrThrow.getWaitForThreads();
        waitForThreads = waitForThreads.toBuilder().setPolicy(failureStrategy).build();
        nodesOrThrow =
                nodesOrThrow.toBuilder().setWaitForThreads(waitForThreads).build();
        threadSpec.putNodes(nodeName, nodesOrThrow);
        return this;
    }
}
