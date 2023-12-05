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

    public UserTaskEventModel() {}

    public UserTaskEventModel(UTETaskExecutedModel executed, Date time) {
        this.executed = executed;
        this.time = time;
        this.type = EventCase.TASK_EXECUTED;
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
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }
}
