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
            throw new InvalidNodeException("UserId can\'t be empty", node);
        }
        if (userGroup != null
                && userGroup.getRhsLiteralValue() != null
                && userGroup.getRhsLiteralValue().getStrVal().trim().isEmpty()) {
            throw new InvalidNodeException("UserGroup can\'t be empty", node);
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

    public String getUserTaskDefName() {
        return this.userTaskDefName;
    }

    public VariableAssignmentModel getUserGroup() {
        return this.userGroup;
    }

    public VariableAssignmentModel getUserId() {
        return this.userId;
    }

    public List<UTActionTriggerModel> getActions() {
        return this.actions;
    }

    public Integer getUserTaskDefVersion() {
        return this.userTaskDefVersion;
    }

    public VariableAssignmentModel getNotes() {
        return this.notes;
    }

    public ReadOnlyMetadataManager getMetadataManager() {
        return this.metadataManager;
    }

    public CoreProcessorContext getProcessorContext() {
        return this.processorContext;
    }

    public VariableAssignmentModel getOnCancellationException() {
        return this.onCancellationException;
    }

    public void setUserTaskDefName(final String userTaskDefName) {
        this.userTaskDefName = userTaskDefName;
    }

    public void setUserGroup(final VariableAssignmentModel userGroup) {
        this.userGroup = userGroup;
    }

    public void setUserId(final VariableAssignmentModel userId) {
        this.userId = userId;
    }

    public void setActions(final List<UTActionTriggerModel> actions) {
        this.actions = actions;
    }

    public void setUserTaskDefVersion(final Integer userTaskDefVersion) {
        this.userTaskDefVersion = userTaskDefVersion;
    }

    public void setNotes(final VariableAssignmentModel notes) {
        this.notes = notes;
    }

    public void setMetadataManager(final ReadOnlyMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void setProcessorContext(final CoreProcessorContext processorContext) {
        this.processorContext = processorContext;
    }

    public void setOnCancellationException(final VariableAssignmentModel onCancellationException) {
        this.onCancellationException = onCancellationException;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserTaskNodeModel)) return false;
        final UserTaskNodeModel other = (UserTaskNodeModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$userTaskDefVersion = this.getUserTaskDefVersion();
        final Object other$userTaskDefVersion = other.getUserTaskDefVersion();
        if (this$userTaskDefVersion == null
                ? other$userTaskDefVersion != null
                : !this$userTaskDefVersion.equals(other$userTaskDefVersion)) return false;
        final Object this$userTaskDefName = this.getUserTaskDefName();
        final Object other$userTaskDefName = other.getUserTaskDefName();
        if (this$userTaskDefName == null
                ? other$userTaskDefName != null
                : !this$userTaskDefName.equals(other$userTaskDefName)) return false;
        final Object this$userGroup = this.getUserGroup();
        final Object other$userGroup = other.getUserGroup();
        if (this$userGroup == null ? other$userGroup != null : !this$userGroup.equals(other$userGroup)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$actions = this.getActions();
        final Object other$actions = other.getActions();
        if (this$actions == null ? other$actions != null : !this$actions.equals(other$actions)) return false;
        final Object this$notes = this.getNotes();
        final Object other$notes = other.getNotes();
        if (this$notes == null ? other$notes != null : !this$notes.equals(other$notes)) return false;
        final Object this$metadataManager = this.getMetadataManager();
        final Object other$metadataManager = other.getMetadataManager();
        if (this$metadataManager == null
                ? other$metadataManager != null
                : !this$metadataManager.equals(other$metadataManager)) return false;
        final Object this$processorContext = this.getProcessorContext();
        final Object other$processorContext = other.getProcessorContext();
        if (this$processorContext == null
                ? other$processorContext != null
                : !this$processorContext.equals(other$processorContext)) return false;
        final Object this$onCancellationException = this.getOnCancellationException();
        final Object other$onCancellationException = other.getOnCancellationException();
        if (this$onCancellationException == null
                ? other$onCancellationException != null
                : !this$onCancellationException.equals(other$onCancellationException)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserTaskNodeModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userTaskDefVersion = this.getUserTaskDefVersion();
        result = result * PRIME + ($userTaskDefVersion == null ? 43 : $userTaskDefVersion.hashCode());
        final Object $userTaskDefName = this.getUserTaskDefName();
        result = result * PRIME + ($userTaskDefName == null ? 43 : $userTaskDefName.hashCode());
        final Object $userGroup = this.getUserGroup();
        result = result * PRIME + ($userGroup == null ? 43 : $userGroup.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $actions = this.getActions();
        result = result * PRIME + ($actions == null ? 43 : $actions.hashCode());
        final Object $notes = this.getNotes();
        result = result * PRIME + ($notes == null ? 43 : $notes.hashCode());
        final Object $metadataManager = this.getMetadataManager();
        result = result * PRIME + ($metadataManager == null ? 43 : $metadataManager.hashCode());
        final Object $processorContext = this.getProcessorContext();
        result = result * PRIME + ($processorContext == null ? 43 : $processorContext.hashCode());
        final Object $onCancellationException = this.getOnCancellationException();
        result = result * PRIME + ($onCancellationException == null ? 43 : $onCancellationException.hashCode());
        return result;
    }
}
