package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.UserTaskAction;
import io.littlehorse.common.model.meta.UserTaskDef;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.UserTaskActionPb;
import io.littlehorse.jlib.common.proto.UserTaskNodePb;
import io.littlehorse.jlib.common.proto.UserTaskNodePb.AssignmentCase;
import io.littlehorse.jlib.common.proto.UserTaskRunPb.AssignedToCase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserTaskNode extends SubNode<UserTaskNodePb> {

    public String userTaskDefName;
    public AssignmentCase assignmentType;
    public VariableAssignment roleGroup;
    public VariableAssignment userId;
    public List<UserTaskAction> actions;

    public UserTaskNode() {
        this.actions = new ArrayList<>();
    }

    public Class<UserTaskNodePb> getProtoBaseClass() {
        return UserTaskNodePb.class;
    }

    public UserTaskNodePb.Builder toProto() {
        UserTaskNodePb.Builder out = UserTaskNodePb
            .newBuilder()
            .setUserTaskDefName(userTaskDefName);

        switch (assignmentType) {
            case ROLE_GROUP:
                out.setRoleGroup(roleGroup.toProto());
                break;
            case USER_ID:
                out.setUserId(userId.toProto());
                break;
            case ASSIGNMENT_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        for (UserTaskAction action : actions) {
            out.addActions(action.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        UserTaskNodePb p = (UserTaskNodePb) proto;
        assignmentType = p.getAssignmentCase();
        userTaskDefName = p.getUserTaskDefName();
        switch (assignmentType) {
            case ROLE_GROUP:
                roleGroup = VariableAssignment.fromProto(p.getRoleGroup());
                break;
            case USER_ID:
                userId = VariableAssignment.fromProto(p.getUserId());
                break;
            case ASSIGNMENT_NOT_SET:
                throw new RuntimeException("not possible");
        }

        for (UserTaskActionPb action : p.getActionsList()) {
            actions.add(LHSerializable.fromProto(action, UserTaskAction.class));
        }
    }

    public UserTaskRun createRun(Date time) {
        UserTaskRun out = new UserTaskRun();
        out.userTaskDefName = userTaskDefName;

        switch (assignmentType) {
            case ROLE_GROUP:
                out.assignedToType = AssignedToCase.GROUPS;
                // need to get groups to assign to.
                // Tried to do it here, but we don't have access to the
                // parent ThreadRun, so we will do it in arrive() instead.
                break;
            case USER_ID:
                out.assignedToType = AssignedToCase.SPECIFIC_USER_ID;
            // same with getting assigned user.

            case ASSIGNMENT_NOT_SET:
            // Not possible, would be validation error.
        }
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        UserTaskDef utd = stores.getUserTaskDef(userTaskDefName, null);

        if (utd == null) {
            throw new LHValidationError(
                null,
                "Specified UserTaskDef " + userTaskDefName + " not found"
            );
        }

        if (assignmentType == null) {
            throw new LHValidationError(
                null,
                "Must specify assignment type for User Task Node"
            );
        }
    }
}
