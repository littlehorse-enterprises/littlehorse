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
import io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;

public class EditUserTaskRunCommentRequestModel extends CoreSubCommand<EditUserTaskRunCommentRequest> {

    private Integer userCommentId;
    private UserTaskRunIdModel userTaskRunId;
    private String userId;
    private String comment;

    @Override
    public UserTaskRun process(CoreProcessorContext executionContext, LHServerConfig config) {
        if (userCommentId == 0) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "The User Comment Id must be provided");
        }
        if (userId.isBlank()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userId must be provided.");
        }
        if (comment.isBlank()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "The comment must be provided.");
        }

        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);

        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }

        if (!utr.getLastEventForComment().containsKey(userCommentId)) {
            throw new LHApiException(
                    Status.NOT_FOUND, "No comment exists for the provided comment ID: " + userCommentId);
        }

        if (utr.getLastEventForComment().get(userCommentId).getType().equals(UserTaskEvent.EventCase.COMMENT_DELETED)) {
            throw new LHApiException(
                    Status.FAILED_PRECONDITION,
                    "The specified comment cannot be edited because it has already been deleted.");
        }

        utr.editComment(userId, comment, userCommentId);

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
    public EditUserTaskRunCommentRequest.Builder toProto() {
        EditUserTaskRunCommentRequest.Builder out = EditUserTaskRunCommentRequest.newBuilder();
        if (userCommentId != null) out.setUserCommentId(userCommentId);
        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());
        if (userId != null) out.setUserId(userId);
        if (comment != null) out.setComment(comment);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        EditUserTaskRunCommentRequest p = (EditUserTaskRunCommentRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        userId = p.getUserId();
        comment = p.getComment();
        userCommentId = p.getUserCommentId();
    }

    @Override
    public Class<EditUserTaskRunCommentRequest> getProtoBaseClass() {
        return EditUserTaskRunCommentRequest.class;
    }
}
