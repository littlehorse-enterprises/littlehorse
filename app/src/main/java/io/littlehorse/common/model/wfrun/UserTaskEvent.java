package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.subnoderun.TaskRun;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.UserTaskEventPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.EventCase;
import java.util.Date;

public class UserTaskEvent extends LHSerializable<UserTaskEventPb> {

    public Date time;
    public EventCase type;
    public TaskRun action;
    public String assignToUser;
    public String assignToRole;

    public Class<UserTaskEventPb> getProtoBaseClass() {
        return UserTaskEventPb.class;
    }

    public UserTaskEventPb.Builder toProto() {
        UserTaskEventPb.Builder out = UserTaskEventPb
            .newBuilder()
            .setTime(LHUtil.fromDate(time));

        switch (type) {
            case ACTION:
                out.setAction(action.toProto());
                break;
            case ASSIGN_TO_ROLE:
                out.setAssignToRole(assignToRole);
                break;
            case ASSIGN_TO_USER:
                out.setAssignToUser(assignToUser);
                break;
            case CANCEL_AND_FAIL:
                out.setCancelAndFail(true);
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
            case ACTION:
                action = TaskRun.fromProto(p.getAction());
                break;
            case ASSIGN_TO_ROLE:
                assignToRole = p.getAssignToRole();
                break;
            case ASSIGN_TO_USER:
                assignToUser = p.getAssignToUser();
                break;
            case CANCEL_AND_FAIL:
                // nothing to do
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }
}
