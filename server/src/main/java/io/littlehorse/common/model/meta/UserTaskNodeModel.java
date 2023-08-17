package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.subnode.UserAssignmentModel;
import io.littlehorse.common.model.meta.usertasks.UTActionTriggerModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.UserTaskNode.AssignmentCase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UserTaskNodeModel extends SubNode<UserTaskNode> {

    private String userTaskDefName;
    private AssignmentCase assignmentType;
    private VariableAssignmentModel userGroup;
    private UserAssignmentModel user;
    private List<UTActionTriggerModel> actions;
    private Integer userTaskDefVersion;
    private VariableAssignmentModel notes;

    public UserTaskNodeModel() {
        this.actions = new ArrayList<>();
    }

    public Class<UserTaskNode> getProtoBaseClass() {
        return UserTaskNode.class;
    }

    public UserTaskNode.Builder toProto() {
        UserTaskNode.Builder out = UserTaskNode
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

        for (UTActionTriggerModel action : actions) {
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
        UserTaskNode p = (UserTaskNode) proto;
        assignmentType = p.getAssignmentCase();
        userTaskDefName = p.getUserTaskDefName();
        switch (assignmentType) {
            case USER_GROUP:
                userGroup = VariableAssignmentModel.fromProto(p.getUserGroup());
                break;
            case USER:
                user =
                    LHSerializable.fromProto(p.getUser(), UserAssignmentModel.class);
                break;
            case ASSIGNMENT_NOT_SET:
                throw new RuntimeException("not possible");
        }

        if (p.hasUserTaskDefVersion()) {
            userTaskDefVersion = p.getUserTaskDefVersion();
        }

        for (UTActionTrigger action : p.getActionsList()) {
            actions.add(LHSerializable.fromProto(action, UTActionTriggerModel.class));
        }

        if (p.hasNotes()) {
            notes =
                LHSerializable.fromProto(p.getNotes(), VariableAssignmentModel.class);
        }
    }

    public List<UTActionTriggerModel> getActions(UTHook requestedHook) {
        List<UTActionTriggerModel> matchingHooks = new ArrayList<>();
        for (UTActionTriggerModel action : actions) {
            if (action.getHook().equals(requestedHook)) {
                matchingHooks.add(action);
            }
        }
        return matchingHooks;
    }

    public UserTaskNodeRunModel createSubNodeRun(Date time) {
        return new UserTaskNodeRunModel();
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        UserTaskDefModel utd = stores.getUserTaskDef(
            userTaskDefName,
            userTaskDefVersion
        );

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
