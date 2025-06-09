package io.littlehorse.common.model.corecommand.subcommand;

import java.util.Date;


import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.EditCommentUserTaskRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;



public class EditCommentUserTaskRunRequestModel extends CoreSubCommand<EditCommentUserTaskRunRequest> {

    private Integer userCommentId ; 
    private UserTaskRunIdModel userTaskRunId;
    private String userId;
    private String comment ;

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        if(userCommentId == null){
            throw new LHApiException(Status.INVALID_ARGUMENT , "The User Comment Id must be provided.");
        }

        if(userId == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The User Id must be provided");
        }

        if (comment == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The comment must be provided");
        }

        if (userTaskRunId == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userTaskRunId can not be null");
        }

        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);

        if (utr == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }
        
        if(!utr.getLastEventForComment().containsKey(userCommentId)){
            throw new LHApiException(Status.INVALID_ARGUMENT, "No comment exists for the provided comment ID: " + userCommentId + ".");
        }

        if(utr.getLastEventForComment().get(userCommentId).getCommentDeleted() != null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The specified comment cannot be edited because it has already been deleted.");
        }
     
        UserTaskEventModel ute = utr.editComment(userId, comment, userCommentId);

        WfRunModel wfRunModel = executionContext.getableManager().get(userTaskRunId.getWfRunId());
        if (wfRunModel == null) {
            throw new LHApiException(Status.DATA_LOSS, "Impossible: got UserTaskRun but missing WfRun");
        }

        wfRunModel.advance(new Date());

        return ute.toProto().build();

    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }

    @Override
    public EditCommentUserTaskRunRequest.Builder toProto() {
        EditCommentUserTaskRunRequest.Builder out = EditCommentUserTaskRunRequest.newBuilder();
        if (userCommentId != null)  out.setUserCommentId(userCommentId);
        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());
        if (userId != null ) out.setUserId(userId);
        if (comment != null) out.setComment(comment);
        return out ;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        EditCommentUserTaskRunRequest p = (EditCommentUserTaskRunRequest) proto;
        userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
        userId = p.getUserId();
        comment = p.getComment();
        userCommentId = p.getUserCommentId();
    }

    @Override
    public Class<EditCommentUserTaskRunRequest> getProtoBaseClass() {
        return EditCommentUserTaskRunRequest.class;
        
    }
}
