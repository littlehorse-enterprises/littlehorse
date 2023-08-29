package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.test.LHClientTestWrapper;
import java.util.function.Consumer;

public class VerifyNodeRunStep implements Step {

    private final int threadRunNumber;
    private final int nodePosition;
    private final Consumer<NodeRun> matcher;

    public VerifyNodeRunStep(int threadRunNumber, int nodePosition, Consumer<NodeRun> matcher) {
        this.threadRunNumber = threadRunNumber;
        this.nodePosition = nodePosition;
        this.matcher = matcher;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        NodeRun nodeRun = lhClientWrapper.getNodeRun(context.toString(), threadRunNumber, nodePosition);
        matcher.accept(nodeRun);
    }
}
