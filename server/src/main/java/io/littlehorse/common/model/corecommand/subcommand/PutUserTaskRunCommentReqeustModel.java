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
import io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class PutUserTaskRunCommentReqeustModel extends CoreSubCommand<PutUserTaskRunCommentRequest> {

    private UserTaskRunIdModel userTaskRunId;
    private String userId;
    private String comment;

    @Override
    public UserTaskRun process(ProcessorExecutionContext executionContext, LHServerConfig config) {
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

        utr.comment(userId, comment);

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
    public PutUserTaskRunCommentRequest.Builder toProto() {
        PutUserTaskRunCommentRequest.Builder out = PutUserTaskRunCommentRequest.newBuilder();
        out.setUserTaskRunId(userTaskRunId.toProto());
        out.setUserId(userId);
        out.setComment(comment);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        PutUserTaskRunCommentRequest p = (PutUserTaskRunCommentRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        userId = p.getUserId();
        comment = p.getComment();
    }

    @Override
    public Class<PutUserTaskRunCommentRequest> getProtoBaseClass() {
        return PutUserTaskRunCommentRequest.class;
    }
}
