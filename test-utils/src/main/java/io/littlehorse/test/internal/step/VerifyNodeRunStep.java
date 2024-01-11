package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.test.internal.TestExecutionContext;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.function.Consumer;

public class VerifyNodeRunStep extends AbstractStep {

    private final int threadRunNumber;
    private final int nodePosition;
    private final Consumer<NodeRun> matcher;

    public VerifyNodeRunStep(int threadRunNumber, int nodePosition, Consumer<NodeRun> matcher, int id) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodePosition = nodePosition;
        this.matcher = matcher;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        NodeRunId id = NodeRunId.newBuilder()
                .setWfRunId(context.getWfRunId())
                .setThreadRunNumber(threadRunNumber)
                .setPosition(nodePosition)
                .build();
        NodeRun nodeRun = lhClient.getNodeRun(id);
        matcher.accept(nodeRun);
    }
}
