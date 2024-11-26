package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.function.Consumer;

public class VerifyLastNodeRunStep extends AbstractStep {

    private final int threadRunNumber;
    private final Consumer<NodeRun> matcher;

    public VerifyLastNodeRunStep(int threadRunNumber, Consumer<NodeRun> matcher, int id) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.matcher = matcher;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        int nodePosition = lhClient.getWfRun(context.getWfRunId())
                .getThreadRuns(threadRunNumber)
                .getCurrentNodePosition();
        NodeRunId id = NodeRunId.newBuilder()
                .setWfRunId(context.getWfRunId())
                .setThreadRunNumber(threadRunNumber)
                .setPosition(nodePosition)
                .build();
        NodeRun nodeRun = lhClient.getNodeRun(id);
        matcher.accept(nodeRun);
    }
}
