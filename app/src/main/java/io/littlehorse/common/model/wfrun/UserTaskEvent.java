package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.UserTaskEventPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.EventCase;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.UTECancelledPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.UTEReassignedPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.UTETaskExecutedPb;
import java.util.Date;

public class UserTaskEvent extends LHSerializable<UserTaskEventPb> {

    public Date time;
    public EventCase type;
    public UTETaskExecutedPb executed;
    public UTEReassignedPb reassigned;
    public UTECancelledPb cancelled;

    public Class<UserTaskEventPb> getProtoBaseClass() {
        return UserTaskEventPb.class;
    }

    public UserTaskEventPb.Builder toProto() {
        UserTaskEventPb.Builder out = UserTaskEventPb
            .newBuilder()
            .setTime(LHUtil.fromDate(time));

        switch (type) {
            case TASK_EXECUTED:
                out.setTaskExecuted(executed);
                break;
            case REASSIGNED:
                out.setReassigned(reassigned);
                break;
            case CANCELLED:
                out.setCancelled(cancelled);
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
                executed = p.getTaskExecuted();
                break;
            case REASSIGNED:
                reassigned = p.getReassigned();
                break;
            case CANCELLED:
                cancelled = p.getCancelled();
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }
}
