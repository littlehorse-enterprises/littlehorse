package io.littlehorse.common.model.corecommand.subcommand;

import java.util.Date;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTECommentDeletedModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.DeleteCommentUserTaskRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;




public class DeleteCommentUserTaskRunRequestModel extends CoreSubCommand<DeleteCommentUserTaskRunRequest> {

    Integer userCommentId;
    private UserTaskRunIdModel userTaskRunId;

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {

        if (userCommentId == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userCommentId must be provided.");
        }

        if (userTaskRunId == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userTaskRunId can not be null");
        }   

        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);

        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }
        
        if(!utr.getLastEventForComment().containsKey(userCommentId)){
            throw new LHApiException(Status.INVALID_ARGUMENT,"The user Comment does not exist");
        }

        utr.deleteComment(userCommentId);

        WfRunModel wfRunModel = executionContext.getableManager().get(userTaskRunId.getWfRunId());
        if (wfRunModel == null) {
            throw new LHApiException(Status.DATA_LOSS, "Impossible: got UserTaskRun but missing WfRun");
        }

        wfRunModel.advance(new Date());
        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }

    @Override
    public DeleteCommentUserTaskRunRequest.Builder toProto() {
        DeleteCommentUserTaskRunRequest.Builder out = DeleteCommentUserTaskRunRequest.newBuilder();
        if (userCommentId != null) out.setUserCommentId(userCommentId);
        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeleteCommentUserTaskRunRequest p = (DeleteCommentUserTaskRunRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        userCommentId = p.getUserCommentId();
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return DeleteCommentUserTaskRunRequest.class;
    }
}
