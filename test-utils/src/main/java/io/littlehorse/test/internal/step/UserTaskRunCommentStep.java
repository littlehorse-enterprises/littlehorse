package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.test.internal.TestExecutionContext;

public class UserTaskRunCommentStep extends AbstractStep {
    private final int threadRunNumber;
    private final int nodeRunNumber;
    private final String userId;
    private final String comment;

    public UserTaskRunCommentStep(int id, int threadRunNumber, int nodeRunNumber, String userId, String comment) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
        this.userId = userId;
        this.comment = comment;
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
            lhClient.putUserTaskRunComment(PutUserTaskRunCommentRequest.newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setUserId(userId)
                    .setComment(comment)
                    .build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Node run %s in thread %s is not a user task", nodeRunNumber, threadRunNumber));
        }
    }
}
