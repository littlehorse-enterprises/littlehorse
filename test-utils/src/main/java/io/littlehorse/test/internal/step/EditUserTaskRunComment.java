package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.test.internal.TestExecutionContext;

public class EditUserTaskRunComment extends AbstractStep {

    private final int threadRunNumber;
    private final int nodeRunNumber;
    private final String userId;
    private final String comment;
    private final int comment_id;

    public EditUserTaskRunComment(
            int id, int threadRunNumber, int nodeRunNumber, String userId, String comment, int comment_id) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
        this.userId = userId;
        this.comment = comment;
        this.comment_id = comment_id;
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
            lhClient.editUserTaskRunComment(EditUserTaskRunCommentRequest.newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setUserId(userId)
                    .setComment(comment)
                    .setUserCommentId(comment_id)
                    .build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Node run %s in thread %s is not a user task", nodeRunNumber, threadRunNumber));
        }
    }
}
