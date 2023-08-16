package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UserTaskEventModel extends LHSerializable<UserTaskEvent> {

    private Date time;
    private EventCase type;
    private UTETaskExecutedModel executed;
    private UTEReassignedModel reassigned;
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

    public UserTaskEventModel(UTEReassignedModel reassigned, Date time) {
        this.reassigned = reassigned;
        this.time = time;
        this.type = EventCase.REASSIGNED;
    }

    public Class<UserTaskEvent> getProtoBaseClass() {
        return UserTaskEvent.class;
    }

    public UserTaskEvent.Builder toProto() {
        UserTaskEvent.Builder out = UserTaskEvent
            .newBuilder()
            .setTime(LHUtil.fromDate(time));

        switch (type) {
            case TASK_EXECUTED:
                out.setTaskExecuted(executed.toProto());
                break;
            case REASSIGNED:
                out.setReassigned(reassigned.toProto());
                break;
            case CANCELLED:
                out.setCancelled(cancelled.toProto());
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskEvent p = (UserTaskEvent) proto;
        time = LHUtil.fromProtoTs(p.getTime());
        type = p.getEventCase();

        switch (type) {
            case TASK_EXECUTED:
                executed =
                    LHSerializable.fromProto(
                        p.getTaskExecuted(),
                        UTETaskExecutedModel.class
                    );
                break;
            case REASSIGNED:
                reassigned =
                    LHSerializable.fromProto(
                        p.getReassigned(),
                        UTEReassignedModel.class
                    );
                break;
            case CANCELLED:
                cancelled =
                    LHSerializable.fromProto(
                        p.getCancelled(),
                        UTECancelledModel.class
                    );
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }
}
