package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UserTaskEventModel extends LHSerializable<UserTaskEvent> {

    private Date time;
    private EventCase type;
    private UTETaskExecutedModel executed;
    private UTEAssignedModel assigned;
    private UTECancelledModel cancelled;
    private UTESavedModel saved;
    private UTECommentedModel commented;
    private UTECommentDeletedModel commentDeleted;
    private UTECommentEditedModel commentEdited;
    private UTECompletedModel completed;

    public UserTaskEventModel() {}

    public UserTaskEventModel(UTETaskExecutedModel executed, Date time) {
        this.executed = executed;
        this.time = time;
        this.type = EventCase.TASK_EXECUTED;
    }

    public UserTaskEventModel(UTESavedModel saved, Date time) {
        this.saved = saved;
        this.time = time;
        this.type = EventCase.SAVED;
    }

    public UserTaskEventModel(UTECancelledModel cancelled, Date time) {
        this.cancelled = cancelled;
        this.time = time;
        this.type = EventCase.CANCELLED;
    }

    public UserTaskEventModel(UTEAssignedModel reassigned, Date time) {
        this.assigned = reassigned;
        this.time = time;
        this.type = EventCase.ASSIGNED;
    }

    public UserTaskEventModel(UTECommentedModel comment, Date time) {
        this.commented = comment;
        this.time = time;
        this.type = EventCase.COMMENT_ADDED;
    }

    public UserTaskEventModel(UTECommentDeletedModel commentDeleted, Date time) {
        this.commentDeleted = commentDeleted;
        this.time = time;
        this.type = EventCase.COMMENT_DELETED;
    }

    public UserTaskEventModel(UTECommentEditedModel commentEdited, Date time) {
        this.commentEdited = commentEdited;
        this.time = time;
        this.type = EventCase.COMMENT_EDITED;
    }

    public UserTaskEventModel(UTECompletedModel completed, Date time) {
        this.completed = completed;
        this.time = time;
        this.type = EventCase.COMPLETED;
    }

    public Class<UserTaskEvent> getProtoBaseClass() {
        return UserTaskEvent.class;
    }

    public UserTaskEvent.Builder toProto() {
        UserTaskEvent.Builder out = UserTaskEvent.newBuilder().setTime(LHUtil.fromDate(time));

        switch (type) {
            case TASK_EXECUTED:
                out.setTaskExecuted(executed.toProto());
                break;
            case ASSIGNED:
                out.setAssigned(assigned.toProto());
                break;
            case CANCELLED:
                out.setCancelled(cancelled.toProto());
                break;
            case SAVED:
                out.setSaved(saved.toProto());
                break;
            case COMMENT_ADDED:
                out.setCommentAdded(commented.toProto());
                break;
            case COMMENT_EDITED:
                out.setCommentEdited(commentEdited.toProto());
                break;
            case COMMENT_DELETED:
                out.setCommentDeleted(commentDeleted.toProto());
                break;
            case COMPLETED:
                out.setCompleted(completed.toProto());
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskEvent p = (UserTaskEvent) proto;
        time = LHUtil.fromProtoTs(p.getTime());
        type = p.getEventCase();

        switch (type) {
            case TASK_EXECUTED:
                executed = LHSerializable.fromProto(p.getTaskExecuted(), UTETaskExecutedModel.class, context);
                break;
            case ASSIGNED:
                assigned = LHSerializable.fromProto(p.getAssigned(), UTEAssignedModel.class, context);
                break;
            case CANCELLED:
                cancelled = LHSerializable.fromProto(p.getCancelled(), UTECancelledModel.class, context);
                break;
            case SAVED:
                saved = LHSerializable.fromProto(p.getSaved(), UTESavedModel.class, context);
                break;
            case COMMENT_ADDED:
                commented = LHSerializable.fromProto(p.getCommentAdded(), UTECommentedModel.class, context);
                break;
            case COMMENT_EDITED:
                commentEdited = LHSerializable.fromProto(p.getCommentEdited(), UTECommentEditedModel.class, context);
                break;
            case COMMENT_DELETED:
                commentDeleted = LHSerializable.fromProto(p.getCommentDeleted(), UTECommentDeletedModel.class, context);
                break;
            case COMPLETED:
                completed = LHSerializable.fromProto(p.getCompleted(), UTECompletedModel.class, context);
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }
}
