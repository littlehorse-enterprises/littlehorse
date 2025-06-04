package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTECommentDeletedModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CommentUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.DeleteCommentUserTaskRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class DeleteCommentUserTaskRunModel extends CoreSubCommand<DeleteCommentUserTaskRunRequest>{

    Integer userCommentId; 
    UserTaskRunIdModel userTaskRunId;

    @Override
    public boolean hasResponse() {
        return false;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {

        if (userCommentId == null ) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userCommentId must be provided.");
        }
        
        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);

        if(utr ==null){
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }
        UTECommentDeletedModel commentDeletedEvent = new UTECommentDeletedModel(userCommentId);



        utr.getEvents().add(new UserTaskEventModel(
                commentDeletedEvent, executionContext.currentCommand().getTime()));
        
        return Empty.getDefaultInstance();


    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }

    @Override
    public DeleteCommentUserTaskRunRequest.Builder toProto() {
        DeleteCommentUserTaskRunRequest.Builder out = DeleteCommentUserTaskRunRequest.newBuilder();
        if (userCommentId != null)  out.setUserCommentId(userCommentId);
        return out ;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        DeleteCommentUserTaskRunRequest p = (DeleteCommentUserTaskRunRequest) proto ;
        userCommentId = p.getUserCommentId();
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
      return DeleteCommentUserTaskRunRequest.class;
    }
    
}
