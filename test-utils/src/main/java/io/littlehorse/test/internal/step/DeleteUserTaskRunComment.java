package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.test.internal.TestExecutionContext;

public class DeleteUserTaskRunComment extends AbstractStep {

    private final int threadRunNumber;
    private final int nodeRunNumber;
    private final int userCommentId;
    private final String userId;

    public DeleteUserTaskRunComment(int id, int threadRunNumber, int nodeRunNumber, int userCommentId, String userId) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
        this.userCommentId = userCommentId;
        this.userId = userId;
    }

    @Override
    void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
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
            lhClient.deleteUserTaskRunComment(DeleteUserTaskRunCommentRequest.newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setUserCommentId(userCommentId)
                    .setUserId(userId)
                    .build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Node run %s in thread %s is not a user task", nodeRunNumber, threadRunNumber));
        }
    }
}
