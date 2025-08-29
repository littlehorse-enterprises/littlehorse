package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UTActionTriggerModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    private CoreProcessorContext processorContext;
    private VariableAssignmentModel onCancellationException;

    public UserTaskNodeModel() {
        this.actions = new ArrayList<>();
    }

    public Class<UserTaskNode> getProtoBaseClass() {
        return UserTaskNode.class;
    }

    public UserTaskNode.Builder toProto() {
        UserTaskNode.Builder out = UserTaskNode.newBuilder().setUserTaskDefName(userTaskDefName);

        if (userId != null) {
            out.setUserId(userId.toProto());
        }
        if (userGroup != null) {
            out.setUserGroup(userGroup.toProto());
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

        if (onCancellationException != null) {
            out.setOnCancellationExceptionName(onCancellationException.toProto());
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
        if (p.hasOnCancellationExceptionName()) {
            onCancellationException = LHSerializable.fromProto(
                    p.getOnCancellationExceptionName(), VariableAssignmentModel.class, context);
        }
        this.metadataManager = context.metadataManager();
        this.processorContext = context.castOnSupport(CoreProcessorContext.class);
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
    public UserTaskNodeRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new UserTaskNodeRunModel(processorContext);
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        UserTaskDefModel utd;
        if (userTaskDefVersion == null) {
            utd = metadataManager.getLastFromPrefix(
                    UserTaskDefIdModel.getPrefix(userTaskDefName), UserTaskDefModel.class);
        } else {
            utd = metadataManager.get(new UserTaskDefIdModel(userTaskDefName, userTaskDefVersion));
        }

        if (utd == null) {
            throw new InvalidNodeException(
                    "Specified UserTaskDef " + userTaskDefName + "/" + userTaskDefVersion + " not found", node);
        }

        // Now pin the version
        userTaskDefVersion = utd.version;

        if (userId == null && userGroup == null) {
            throw new InvalidNodeException("Must specify userGroup or userId", node);
        }

        if (userId != null
                && userId.getRhsLiteralValue() != null
                && userId.getRhsLiteralValue().getStrVal().trim().isEmpty()) {
            throw new InvalidNodeException("UserId can't be empty", node);
        }

        if (userGroup != null
                && userGroup.getRhsLiteralValue() != null
                && userGroup.getRhsLiteralValue().getStrVal().trim().isEmpty()) {
            throw new InvalidNodeException("UserGroup can't be empty", node);
        }
    }

    public String assignExceptionNameVariable(ThreadRunModel threadRun) {
        try {
            if (onCancellationException != null) {
                String resolvedExceptionName = threadRun
                        .assignVariable(onCancellationException)
                        .asStr()
                        .getStrVal();
                if (!Strings.isNullOrEmpty(resolvedExceptionName) && !resolvedExceptionName.isBlank()) {
                    return resolvedExceptionName;
                } else {
                    return LHConstants.USER_TASK_CANCELLED;
                }
            } else {
                return LHConstants.USER_TASK_CANCELLED;
            }
        } catch (LHVarSubError e) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Must specify a valid on cancel exception name");
        }
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        // TODO (#1575): create a strong structure for user task outputs
        return Optional.of(new ReturnTypeModel(VariableType.JSON_OBJ));
    }
}
