package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.usertasks.UTActionTrigger;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.UTActionTriggerPb;
import io.littlehorse.jlib.common.proto.UserTaskNodePb;
import io.littlehorse.jlib.common.proto.UserTaskNodePb.AssignmentCase;
import io.littlehorse.jlib.common.proto.UserTaskRunPb.AssignedToCase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserTaskNode extends SubNode<UserTaskNodePb> {

    private String userTaskDefName;
    private AssignmentCase assignmentType;
    private VariableAssignment userGroup;
    private VariableAssignment userId;
    private List<UTActionTrigger> actions;
    private Integer userTaskDefVersion;
    private VariableAssignment notes;

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
            case USER_GROUP:
                out.setUserGroup(userGroup.toProto());
                break;
            case USER_ID:
                out.setUserId(userId.toProto());
                break;
            case ASSIGNMENT_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        for (UTActionTrigger action : actions) {
            out.addActions(action.toProto());
        }

        if (userTaskDefVersion != null) {
            out.setUserTaskDefVersion(userTaskDefVersion);
        }

        if (notes != null) {
            out.setNotes(notes.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        UserTaskNodePb p = (UserTaskNodePb) proto;
        assignmentType = p.getAssignmentCase();
        userTaskDefName = p.getUserTaskDefName();
        switch (assignmentType) {
            case USER_GROUP:
                userGroup = VariableAssignment.fromProto(p.getUserGroup());
                break;
            case USER_ID:
                userId = VariableAssignment.fromProto(p.getUserId());
                break;
            case ASSIGNMENT_NOT_SET:
                throw new RuntimeException("not possible");
        }

        if (p.hasUserTaskDefVersion()) {
            userTaskDefVersion = p.getUserTaskDefVersion();
        }

        for (UTActionTriggerPb action : p.getActionsList()) {
            actions.add(LHSerializable.fromProto(action, UTActionTrigger.class));
        }

        if (p.hasNotes()) {
            notes = LHSerializable.fromProto(p.getNotes(), VariableAssignment.class);
        }
    }

    public UserTaskRun createSubNodeRun(Date time) {
        UserTaskRun out = new UserTaskRun();
        out.userTaskDefName = userTaskDefName;
        out.userTaskDefVersion = userTaskDefVersion;

        switch (assignmentType) {
            case USER_GROUP:
                out.assignedToType = AssignedToCase.USER_GROUP;
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
        UserTaskDef utd = stores.getUserTaskDef(userTaskDefName, userTaskDefVersion);

        if (utd == null) {
            throw new LHValidationError(
                null,
                "Specified UserTaskDef " +
                userTaskDefName +
                "/" +
                userTaskDefVersion +
                " not found"
            );
        }

        // Now pin the version
        userTaskDefVersion = utd.version;

        if (assignmentType == null) {
            throw new LHValidationError(
                null,
                "Must specify assignment type for User Task Node"
            );
        }
    }
}
