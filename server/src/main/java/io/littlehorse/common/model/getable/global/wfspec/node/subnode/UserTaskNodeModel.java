package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UTActionTriggerModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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
    private VariableAssignmentModel userGroup;
    private VariableAssignmentModel userId;
    private List<UTActionTriggerModel> actions;
    private Integer userTaskDefVersion;
    private VariableAssignmentModel notes;
    private ReadOnlyMetadataManager metadataManager;
    private ProcessorExecutionContext processorContext;

    public UserTaskNodeModel() {
        this.actions = new ArrayList<>();
    }

    public Class<UserTaskNode> getProtoBaseClass() {
        return UserTaskNode.class;
    }

    public UserTaskNode.Builder toProto() {
        UserTaskNode.Builder out = UserTaskNode.newBuilder().setUserTaskDefName(userTaskDefName);

        if (userId != null) out.setUserId(userId.toProto());
        if (userGroup != null) out.setUserGroup(userGroup.toProto());

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

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskNode p = (UserTaskNode) proto;
        userTaskDefName = p.getUserTaskDefName();
        if (p.hasUserGroup()) userGroup = VariableAssignmentModel.fromProto(p.getUserGroup(), context);
        if (p.hasUserId()) userId = VariableAssignmentModel.fromProto(p.getUserId(), context);

        if (p.hasUserTaskDefVersion()) {
            userTaskDefVersion = p.getUserTaskDefVersion();
        }

        for (UTActionTrigger action : p.getActionsList()) {
            actions.add(LHSerializable.fromProto(action, UTActionTriggerModel.class, context));
        }

        if (p.hasNotes()) {
            notes = LHSerializable.fromProto(p.getNotes(), VariableAssignmentModel.class, context);
        }
        this.metadataManager = context.metadataManager();
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
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

    @Override
    public UserTaskNodeRunModel createSubNodeRun(Date time) {
        return new UserTaskNodeRunModel(processorContext);
    }

    @Override
    public void validate() throws LHApiException {
        UserTaskDefModel utd;
        if (userTaskDefVersion == null) {
            utd = metadataManager.getLastFromPrefix(
                    UserTaskDefIdModel.getPrefix(userTaskDefName), UserTaskDefModel.class);
        } else {
            utd = metadataManager.get(new UserTaskDefIdModel(userTaskDefName, userTaskDefVersion));
        }

        if (utd == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Specified UserTaskDef " + userTaskDefName + "/" + userTaskDefVersion + " not found");
        }

        // Now pin the version
        userTaskDefVersion = utd.version;

        if (userId == null && userGroup == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Must specify userGroup or userId");
        }
    }
}
