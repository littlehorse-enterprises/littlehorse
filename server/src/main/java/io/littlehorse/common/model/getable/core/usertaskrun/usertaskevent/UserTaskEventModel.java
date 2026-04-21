package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserTaskEventModel)) return false;
        final UserTaskEventModel other = (UserTaskEventModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$time = this.getTime();
        final Object other$time = other.getTime();
        if (this$time == null ? other$time != null : !this$time.equals(other$time)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$executed = this.getExecuted();
        final Object other$executed = other.getExecuted();
        if (this$executed == null ? other$executed != null : !this$executed.equals(other$executed)) return false;
        final Object this$assigned = this.getAssigned();
        final Object other$assigned = other.getAssigned();
        if (this$assigned == null ? other$assigned != null : !this$assigned.equals(other$assigned)) return false;
        final Object this$cancelled = this.getCancelled();
        final Object other$cancelled = other.getCancelled();
        if (this$cancelled == null ? other$cancelled != null : !this$cancelled.equals(other$cancelled)) return false;
        final Object this$saved = this.getSaved();
        final Object other$saved = other.getSaved();
        if (this$saved == null ? other$saved != null : !this$saved.equals(other$saved)) return false;
        final Object this$commented = this.getCommented();
        final Object other$commented = other.getCommented();
        if (this$commented == null ? other$commented != null : !this$commented.equals(other$commented)) return false;
        final Object this$commentDeleted = this.getCommentDeleted();
        final Object other$commentDeleted = other.getCommentDeleted();
        if (this$commentDeleted == null
                ? other$commentDeleted != null
                : !this$commentDeleted.equals(other$commentDeleted)) return false;
        final Object this$commentEdited = this.getCommentEdited();
        final Object other$commentEdited = other.getCommentEdited();
        if (this$commentEdited == null ? other$commentEdited != null : !this$commentEdited.equals(other$commentEdited))
            return false;
        final Object this$completed = this.getCompleted();
        final Object other$completed = other.getCompleted();
        if (this$completed == null ? other$completed != null : !this$completed.equals(other$completed)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserTaskEventModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $time = this.getTime();
        result = result * PRIME + ($time == null ? 43 : $time.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $executed = this.getExecuted();
        result = result * PRIME + ($executed == null ? 43 : $executed.hashCode());
        final Object $assigned = this.getAssigned();
        result = result * PRIME + ($assigned == null ? 43 : $assigned.hashCode());
        final Object $cancelled = this.getCancelled();
        result = result * PRIME + ($cancelled == null ? 43 : $cancelled.hashCode());
        final Object $saved = this.getSaved();
        result = result * PRIME + ($saved == null ? 43 : $saved.hashCode());
        final Object $commented = this.getCommented();
        result = result * PRIME + ($commented == null ? 43 : $commented.hashCode());
        final Object $commentDeleted = this.getCommentDeleted();
        result = result * PRIME + ($commentDeleted == null ? 43 : $commentDeleted.hashCode());
        final Object $commentEdited = this.getCommentEdited();
        result = result * PRIME + ($commentEdited == null ? 43 : $commentEdited.hashCode());
        final Object $completed = this.getCompleted();
        result = result * PRIME + ($completed == null ? 43 : $completed.hashCode());
        return result;
    }

    public Date getTime() {
        return this.time;
    }

    public EventCase getType() {
        return this.type;
    }

    public UTETaskExecutedModel getExecuted() {
        return this.executed;
    }

    public UTEAssignedModel getAssigned() {
        return this.assigned;
    }

    public UTECancelledModel getCancelled() {
        return this.cancelled;
    }

    public UTESavedModel getSaved() {
        return this.saved;
    }

    public UTECommentedModel getCommented() {
        return this.commented;
    }

    public UTECommentDeletedModel getCommentDeleted() {
        return this.commentDeleted;
    }

    public UTECommentEditedModel getCommentEdited() {
        return this.commentEdited;
    }

    public UTECompletedModel getCompleted() {
        return this.completed;
    }

    public void setTime(final Date time) {
        this.time = time;
    }

    public void setType(final EventCase type) {
        this.type = type;
    }

    public void setExecuted(final UTETaskExecutedModel executed) {
        this.executed = executed;
    }

    public void setAssigned(final UTEAssignedModel assigned) {
        this.assigned = assigned;
    }

    public void setCancelled(final UTECancelledModel cancelled) {
        this.cancelled = cancelled;
    }

    public void setSaved(final UTESavedModel saved) {
        this.saved = saved;
    }

    public void setCommented(final UTECommentedModel commented) {
        this.commented = commented;
    }

    public void setCommentDeleted(final UTECommentDeletedModel commentDeleted) {
        this.commentDeleted = commentDeleted;
    }

    public void setCommentEdited(final UTECommentEditedModel commentEdited) {
        this.commentEdited = commentEdited;
    }

    public void setCompleted(final UTECompletedModel completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "UserTaskEventModel(time=" + this.getTime() + ", type=" + this.getType() + ", executed="
                + this.getExecuted() + ", assigned=" + this.getAssigned() + ", cancelled=" + this.getCancelled()
                + ", saved=" + this.getSaved() + ", commented=" + this.getCommented() + ", commentDeleted="
                + this.getCommentDeleted() + ", commentEdited=" + this.getCommentEdited() + ", completed="
                + this.getCompleted() + ")";
    }
}
