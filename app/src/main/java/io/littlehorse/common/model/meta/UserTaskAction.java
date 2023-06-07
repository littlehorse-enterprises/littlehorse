package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.jlib.common.proto.UserTaskActionPb;
import io.littlehorse.jlib.common.proto.UserTaskActionPb.ActionCase;

public class UserTaskAction extends LHSerializable<UserTaskActionPb> {

    public ActionCase type;
    public TaskNode task;
    public VariableAssignment assignToRole;
    public VariableAssignment assignToUser;

    public VariableAssignment delaySeconds;

    public Class<UserTaskActionPb> getProtoBaseClass() {
        return UserTaskActionPb.class;
    }

    public UserTaskActionPb.Builder toProto() {
        UserTaskActionPb.Builder out = UserTaskActionPb.newBuilder();

        switch (type) {
            case TASK:
                out.setTask(task.toProto());
                break;
            case ASSIGN_TO_ROLE:
                out.setAssignToRole(assignToRole.toProto());
                break;
            case ASSIGN_TO_USER:
                out.setAssignToUser(assignToUser.toProto());
                break;
            case CANCEL_AND_FAIL:
                out.setCancelAndFail(true);
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        if (delaySeconds != null) {
            out.setDelaySeconds(delaySeconds.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskActionPb p = (UserTaskActionPb) proto;
        if (p.hasDelaySeconds()) {
            delaySeconds = VariableAssignment.fromProto(p.getDelaySeconds());
        }

        type = p.getActionCase();
        switch (type) {
            case TASK:
                task = LHSerializable.fromProto(p.getTask(), TaskNode.class);
                break;
            case ASSIGN_TO_ROLE:
                assignToRole = VariableAssignment.fromProto(p.getAssignToRole());
                break;
            case ASSIGN_TO_USER:
                assignToUser = VariableAssignment.fromProto(p.getAssignToUser());
                break;
            case CANCEL_AND_FAIL:
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
