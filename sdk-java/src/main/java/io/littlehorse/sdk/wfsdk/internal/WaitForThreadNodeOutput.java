package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.WaitForThreadsFailureStrategy;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;

public class WaitForThreadNodeOutput extends NodeOutputImpl {

    private final ThreadSpec.Builder threadSpec;

    public WaitForThreadNodeOutput(String nodeName, ThreadBuilderImpl parent, ThreadSpec.Builder threadSpec) {
        super(nodeName, parent);
        this.threadSpec = threadSpec;
    }

    public WaitForThreadNodeOutput withFailureStrategy(WaitForThreadsFailureStrategy failureStrategy) {
        Node nodesOrThrow = threadSpec.getNodesOrThrow(nodeName);
        WaitForThreadsNode waitForThreads = nodesOrThrow.getWaitForThreads();
        waitForThreads =
                waitForThreads.toBuilder().setFailureStrategy(failureStrategy).build();
        nodesOrThrow =
                nodesOrThrow.toBuilder().setWaitForThreads(waitForThreads).build();
        threadSpec.putNodes(nodeName, nodesOrThrow);
        return this;
    }
}
