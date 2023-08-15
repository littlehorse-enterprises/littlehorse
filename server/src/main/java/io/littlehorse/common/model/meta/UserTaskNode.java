package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.subnode.UserAssignment;
import io.littlehorse.common.model.meta.usertasks.UTActionTrigger;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskNodeRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskNodePb;
import io.littlehorse.sdk.common.proto.UserTaskNodePb.AssignmentCase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UserTaskNode extends SubNode<UserTaskNodePb> {

    private String userTaskDefName;
    private AssignmentCase assignmentType;
    private VariableAssignment userGroup;
    private UserAssignment user;
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
            case USER:
                out.setUser(user.toProto());
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
            case USER:
                user = LHSerializable.fromProto(p.getUser(), UserAssignment.class);
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

    public List<UTActionTrigger> getActions(UTHook requestedHook) {
        List<UTActionTrigger> matchingHooks = new ArrayList<>();
        for (UTActionTrigger action : actions) {
            if (action.getHook().equals(requestedHook)) {
                matchingHooks.add(action);
            }
        }
        return matchingHooks;
    }

    public UserTaskNodeRun createSubNodeRun(Date time) {
        return new UserTaskNodeRun();
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
