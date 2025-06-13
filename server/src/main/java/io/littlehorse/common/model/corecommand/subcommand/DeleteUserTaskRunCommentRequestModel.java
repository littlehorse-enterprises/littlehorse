package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class DeleteUserTaskRunCommentRequestModel extends CoreSubCommand<DeleteUserTaskRunCommentRequest> {

    Integer userCommentId;
    private UserTaskRunIdModel userTaskRunId;
    private String userId;

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public UserTaskRun process(ProcessorExecutionContext executionContext, LHServerConfig config) {

        if (userCommentId == 0) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "The User Comment Id must be provided");
        }

        if (userId.isBlank()) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "The User Id must be provide");
        }

        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);

        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }

        if (!utr.getLastEventForComment().containsKey(userCommentId)) {
            throw new LHApiException(Status.NOT_FOUND, "The user Comment does not exist");
        }

        if (utr.getLastEventForComment().get(userCommentId).getType().equals(UserTaskEvent.EventCase.COMMENT_DELETED)) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "The specified comment cannot be deleted because it has already been deleted.");
        }

        utr.deleteComment(userCommentId, userId);

        WfRunModel wfRunModel = executionContext.getableManager().get(userTaskRunId.getWfRunId());
        if (wfRunModel == null) {
            throw new LHApiException(Status.DATA_LOSS, "Impossible: got UserTaskRun but missing WfRun");
        }

        return utr.toProto().build();
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }

    @Override
    public DeleteUserTaskRunCommentRequest.Builder toProto() {
        DeleteUserTaskRunCommentRequest.Builder out = DeleteUserTaskRunCommentRequest.newBuilder();
        if (userCommentId != null) out.setUserCommentId(userCommentId);
        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());
        if (userId != null) out.setUserId(userId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeleteUserTaskRunCommentRequest p = (DeleteUserTaskRunCommentRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        userCommentId = p.getUserCommentId();
        userId = p.getUserId();
    }

    @Override
    public Class<DeleteUserTaskRunCommentRequest> getProtoBaseClass() {
        return DeleteUserTaskRunCommentRequest.class;
    }
}
