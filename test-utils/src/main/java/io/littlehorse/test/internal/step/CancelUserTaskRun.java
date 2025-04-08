package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.test.internal.TestExecutionContext;

public class CancelUserTaskRun extends AbstractStep {
    private final int threadRunNumber;
    private final int nodeRunNumber;

    public CancelUserTaskRun(int id, int threadRunNumber, int nodeRunNumber) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
    }

    @Override
    void tryExecute(TestExecutionContext context, LittleHorseGrpc.LittleHorseBlockingStub lhClient) {
        NodeRunId nodeId = NodeRunId.newBuilder()
                .setWfRunId(context.getWfRunId())
                .setThreadRunNumber(threadRunNumber)
                .setPosition(nodeRunNumber)
                .build();
        NodeRun nodeRun = lhClient.getNodeRun(nodeId);
        if (nodeRun.hasUserTask()) {
            String userTaskGuid = nodeRun.getUserTask().getUserTaskRunId().getUserTaskGuid();
            UserTaskRunId userTaskId = UserTaskRunId.newBuilder()
                    .setWfRunId(context.getWfRunId())
                    .setUserTaskGuid(userTaskGuid)
                    .build();
            lhClient.cancelUserTaskRun(CancelUserTaskRunRequest.newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Node run %s in thread %s is not a user task", nodeRunNumber, threadRunNumber));
        }
    }
}
