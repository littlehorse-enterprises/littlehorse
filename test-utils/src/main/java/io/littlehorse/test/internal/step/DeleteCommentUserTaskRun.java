package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.DeleteCommentUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.test.internal.TestExecutionContext;

public class DeleteCommentUserTaskRun extends AbstractStep{

    private final int threadRunNumber;
    private final int nodeRunNumber;
    private final int userCommentId;

    public DeleteCommentUserTaskRun(int id, int threadRunNumber, int nodeRunNumber, int userCommentId) {
        super(id);
        this.threadRunNumber = threadRunNumber;
        this.nodeRunNumber = nodeRunNumber;
        this.userCommentId = userCommentId;
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
            lhClient.deleteCommentUserTaskRun(DeleteCommentUserTaskRunRequest.newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setUserCommentId(userCommentId)
                    .build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Node run %s in thread %s is not a user task", nodeRunNumber, threadRunNumber));
        }
    }

}


