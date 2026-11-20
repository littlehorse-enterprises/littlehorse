package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.model.getable.global.formdef.FormDefModel;
import io.littlehorse.common.model.getable.global.formdef.FormDefReferenceModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UTActionTriggerModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private Map<String, VariableAssignmentModel> placeholders = new HashMap<>();

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

        for (Map.Entry<String, VariableAssignmentModel> entry : placeholders.entrySet()) {
            out.putPlaceholders(entry.getKey(), entry.getValue().toProto().build());
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
        for (Map.Entry<String, VariableAssignment> entry :
                p.getPlaceholdersMap().entrySet()) {
            placeholders.put(entry.getKey(), VariableAssignmentModel.fromProto(entry.getValue(), context));
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
    public void validate(ExecutionContext ctx) throws InvalidNodeException {
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

        validatePlaceholders(utd);
    }

    private void validatePlaceholders(UserTaskDefModel utd) throws InvalidNodeException {
        FormDefReferenceModel ref = utd.getFormDef();
        if (ref == null) {
            if (!placeholders.isEmpty()) {
                throw new InvalidNodeException("Placeholders require a UserTaskDef with a FormDef", node);
            }
            return;
        }
        FormDefModel formDef = new WfService(metadataManager).getFormDef(ref);
        if (formDef == null) {
            throw new InvalidNodeException("FormDef " + ref.getName() + " not found", node);
        }
        Set<String> required = formDef.getPlaceholderNames();
        Set<String> missing = new HashSet<>(required);
        missing.removeAll(placeholders.keySet());
        if (!missing.isEmpty()) {
            throw new InvalidNodeException("Missing FormDef placeholders: " + missing, node);
        }
        Set<String> unused = new HashSet<>(placeholders.keySet());
        unused.removeAll(required);
        if (!unused.isEmpty()) {
            throw new InvalidNodeException("Placeholders not referenced by FormDef: " + unused, node);
        }
        for (Map.Entry<String, VariableAssignmentModel> entry : placeholders.entrySet()) {
            if (exposesMaskedValue(entry.getValue())) {
                throw new InvalidNodeException(
                        "Placeholder %s exposes a masked value. Unmask it or pass a reference instead"
                                .formatted(entry.getKey()),
                        node);
            }
        }
    }

    private boolean exposesMaskedValue(VariableAssignmentModel assignment) throws InvalidNodeException {
        ThreadSpecModel threadSpec = node.getThreadSpec();
        try {
            return assignment.readsMaskedValue(metadataManager, threadSpec.getWfSpec(), threadSpec.getName());
        } catch (InvalidExpressionException exn) {
            throw new InvalidNodeException("Invalid placeholder assignment: " + exn.getMessage(), node);
        }
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assignment : placeholders.values()) {
            out.addAll(assignment.getRequiredWfRunVarNames());
        }
        return out;
    }

    @Override
    public Set<String> getNeededNodeNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assignment : placeholders.values()) {
            out.addAll(assignment.getRequiredNodeNames());
        }
        return out;
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
        UserTaskDefModel userTaskDef = manager.get(new UserTaskDefIdModel(userTaskDefName, userTaskDefVersion));
        if (userTaskDef == null) {
            return Optional.empty();
        }
        if (userTaskDef.getResultStructDefId() != null) {
            return Optional.of(new ReturnTypeModel(new TypeDefinitionModel(userTaskDef.getResultStructDefId())));
        }
        if (!userTaskDef.getFields().isEmpty()) {
            return Optional.of(new ReturnTypeModel(VariableType.JSON_OBJ));
        }
        return Optional.of(new ReturnTypeModel());
    }
}
