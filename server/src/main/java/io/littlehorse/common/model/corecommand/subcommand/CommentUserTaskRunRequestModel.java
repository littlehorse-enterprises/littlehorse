package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTECommentedModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CommentUserTaskRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentUserTaskRunRequestModel extends CoreSubCommand<CommentUserTaskRunRequest> {

    private UserTaskRunIdModel userTaskRunId;
    private String userId ;
    private String comment ;

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        if (userTaskRunId == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userTaskRunId must be provided.");
        }

        if (userId == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The userId must be provided.");
        }

        if (comment == null){
            throw new LHApiException(Status.INVALID_ARGUMENT, "The comment must be provided.");
        }

        UserTaskRunModel utr = executionContext.getableManager().get(userTaskRunId);

        if (utr == null){
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskRun " + userTaskRunId);
        }   

        Integer commentIdCounter = utr.getCommentIdCounter();
        UTECommentedModel commentedEvent = new UTECommentedModel(userId, comment, commentIdCounter);
        utr.setCommentIdCounter(commentIdCounter + 1);

        utr.getEvents().add(new UserTaskEventModel(commentedEvent, executionContext.currentCommand().getTime()));

        WfRunModel wfRunModel = executionContext.getableManager().get(userTaskRunId.getWfRunId());
        if (wfRunModel == null) {
            throw new LHApiException(Status.DATA_LOSS, "Impossible: got UserTaskRun but missing WfRun");
        }

        wfRunModel.advance(new Date());

        return commentedEvent.toProto().build();
    }

    @Override
    public String getPartitionKey() {
        return userTaskRunId.getPartitionKey().get();
    }

    @Override
    public CommentUserTaskRunRequest.Builder toProto() {
        CommentUserTaskRunRequest.Builder out=  CommentUserTaskRunRequest.newBuilder();
        out.setUserTaskRunId(userTaskRunId.toProto());
        out.setUserId(userId);
        out.setComment(comment);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
       CommentUserTaskRunRequest p = (CommentUserTaskRunRequest) proto;
       userTaskRunId = LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class, context);
       userId = p.getUserId();
       comment = p.getComment();
    }

    @Override
    public Class<CommentUserTaskRunRequest> getProtoBaseClass() {
       return CommentUserTaskRunRequest.class;
    }
}
