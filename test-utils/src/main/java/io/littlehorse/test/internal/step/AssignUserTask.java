package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.test.internal.TestExecutionContext;

public class AssignUserTask extends AbstractStep {

    private final boolean overrideClaim;
    private final String userId;
    private final String groupId;
    private final int threadRunNumber;
    private final int nodeRunNumber;

    public AssignUserTask(
            int id, int threadRunNumber, int nodeRunNumber, boolean overrideClaim, String userId, String groupId) {
        super(id);
        this.overrideClaim = overrideClaim;
        this.userId = userId;
        this.groupId = groupId;
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
            AssignUserTaskRunRequest.Builder requestBuilder = AssignUserTaskRunRequest.newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setOverrideClaim(overrideClaim);
            if (userId != null) {
                requestBuilder.setUserId(userId);
            }

            if (groupId != null) {
                requestBuilder.setUserGroup(groupId);
            }

            lhClient.assignUserTaskRun(requestBuilder.build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Node run %s in thread %s is not a user task", nodeRunNumber, threadRunNumber));
        }
    }
}
