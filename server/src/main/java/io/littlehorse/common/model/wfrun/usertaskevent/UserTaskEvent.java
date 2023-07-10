package io.littlehorse.common.model.wfrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.UserTaskEventPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.EventCase;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class UserTaskEvent extends LHSerializable<UserTaskEventPb> {

    private Date time;
    private EventCase type;
    private UTETaskExecuted executed;
    private UTEReassigned reassigned;
    private UTECancelled cancelled;

    public UserTaskEvent() {}

    public UserTaskEvent(UTETaskExecuted executed, Date time) {
        this.executed = executed;
        this.time = time;
        this.type = EventCase.TASK_EXECUTED;
    }

    public UserTaskEvent(UTECancelled cancelled, Date time) {
        this.cancelled = cancelled;
        this.time = time;
        this.type = EventCase.CANCELLED;
    }

    public UserTaskEvent(UTEReassigned reassigned, Date time) {
        this.reassigned = reassigned;
        this.time = time;
        this.type = EventCase.REASSIGNED;
    }

    public Class<UserTaskEventPb> getProtoBaseClass() {
        return UserTaskEventPb.class;
    }

    public UserTaskEventPb.Builder toProto() {
        UserTaskEventPb.Builder out = UserTaskEventPb
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
        UserTaskEventPb p = (UserTaskEventPb) proto;
        time = LHUtil.fromProtoTs(p.getTime());
        type = p.getEventCase();

        switch (type) {
            case TASK_EXECUTED:
                executed =
                    LHSerializable.fromProto(
                        p.getTaskExecuted(),
                        UTETaskExecuted.class
                    );
                break;
            case REASSIGNED:
                reassigned =
                    LHSerializable.fromProto(p.getReassigned(), UTEReassigned.class);
                break;
            case CANCELLED:
                cancelled =
                    LHSerializable.fromProto(p.getCancelled(), UTECancelled.class);
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }
}
