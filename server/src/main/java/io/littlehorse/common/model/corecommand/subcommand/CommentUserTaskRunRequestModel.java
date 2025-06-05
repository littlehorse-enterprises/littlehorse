package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
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
import java.util.List;

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
    public Empty process(ProcessorExecutionContext executionContext, LHServerConfig config) {
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

        // Integer commentIdCounter = utr.getCommentIdCounter();
        // UTECommentedModel commentedEvent = new UTECommentedModel(userId, comment, commentIdCounter);
        // utr.setCommentIdCounter(commentIdCounter + 1);

        // System.out.println("CommentedEvent : " + commentedEvent.getUserId() + " " +  commentedEvent.getComment());
        // UserTaskEventModel ute =  new UserTaskEventModel(commentedEvent, executionContext.currentCommand().getTime());
        // System.out.println("UTE to json " + ute.toJson());
        // System.out.println("UTE commented to json:" + ute.getCommented().toJson());
        // List<UserTaskEventModel> list = utr.getEvents();
        // list.add(ute);
        // utr.setEvents(list);
        // System.out.println("list size " + list.size());
        // System.out.println("the first comment in json:" + list.get(1).toJson());
        System.out.println("Size of list b4" + utr.getEvents().size());
        utr.commented(userId, comment);
        System.out.println("Size of list after" + utr.getEvents().size());


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
