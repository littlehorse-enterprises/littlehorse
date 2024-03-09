package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.TaskNodeRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.ExponentialBackoffRetryPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.sdk.common.proto.TaskNode;
import io.littlehorse.sdk.common.proto.TaskNode.RetryPolicyCase;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNodeModel extends SubNode<TaskNode> {

    private TaskDefIdModel taskDefId;
    private List<VariableAssignmentModel> variables;
    private int timeoutSeconds;

    private TaskDefModel taskDef;
    private WfService wfService;
    private ReadOnlyMetadataManager metadataManager;
    private ProcessorExecutionContext processorContext;

    private RetryPolicyCase retryPolicyType;
    private Integer simpleRetries;
    private ExponentialBackoffRetryPolicyModel exponentialBackoffRetryPolicy;

    public TaskDefModel getTaskDef() {
        if (taskDef == null) {
            taskDef = metadataManager.get(taskDefId);
        }
        return taskDef;
    }

    private NodeModel node;

    public TaskNodeModel() {
        variables = new ArrayList<>();
        this.retryPolicyType = RetryPolicyCase.RETRYPOLICY_NOT_SET;
    }

    public void setNode(NodeModel node) {
        this.node = node;
    }

    public Class<TaskNode> getProtoBaseClass() {
        return TaskNode.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskNode p = (TaskNode) proto;
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);

        timeoutSeconds = p.getTimeoutSeconds();
        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }

        for (VariableAssignment assn : p.getVariablesList()) {
            variables.add(VariableAssignmentModel.fromProto(assn, context));
        }
        this.metadataManager = context.metadataManager();
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);

        this.retryPolicyType = p.getRetryPolicyCase();
        switch (retryPolicyType) {
            case SIMPLE_RETRIES:
                simpleRetries = p.getSimpleRetries();
                break;
            case EXPONENTIAL_BACKOFF:
                exponentialBackoffRetryPolicy = LHSerializable.fromProto(
                        p.getExponentialBackoff(), ExponentialBackoffRetryPolicyModel.class, context);
                break;
            case RETRYPOLICY_NOT_SET:
                break;
        }
    }

    public TaskNode.Builder toProto() {
        TaskNode.Builder out =
                TaskNode.newBuilder().setTaskDefId(taskDefId.toProto()).setTimeoutSeconds(timeoutSeconds);

        for (VariableAssignmentModel va : variables) {
            out.addVariables(va.toProto());
        }

        switch (retryPolicyType) {
            case SIMPLE_RETRIES:
                out.setSimpleRetries(simpleRetries);
                break;
            case EXPONENTIAL_BACKOFF:
                out.setExponentialBackoff(exponentialBackoffRetryPolicy.toProto());
                break;
            case RETRYPOLICY_NOT_SET:
                break;
        }
        return out;
    }

    @Override
    public void validate() throws LHApiException {
        // Want to be able to release new versions of taskdef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the taskdef to ensure that this isn't an issue.
        TaskDefModel taskDef = metadataManager.get(new TaskDefIdModel(taskDefId.getName()));
        if (taskDef == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Refers to nonexistent TaskDef " + taskDefId);
        }

        // TODO: Validate retry policy; ensure that it is sensible.

        // Now need to validate that all of the variables are provided.
        if (variables.size() != taskDef.inputVars.size()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "For TaskDef "
                            + taskDef.getName()
                            + " we need "
                            + taskDef.inputVars.size()
                            + " input vars, but we have "
                            + variables.size());
        }

        // Currently, we don't do any type-checking for JSON_ARR or JSON_OBJ variables
        // because they are not strongly-typed. Future versions of LittleHorse will
        // include the ability to register a schema for JSON Variables. For strongly-
        // typed JSON variables (i.e. those with a schema), we will also validate
        // those as well.
        for (int i = 0; i < variables.size(); i++) {
            VariableDefModel taskDefVar = taskDef.getInputVars().get(i);
            VariableAssignmentModel assn = variables.get(i);
            if (!assn.canBeType(taskDefVar.getType(), this.node.getThreadSpec())) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Input variable " + i + " needs to be " + taskDefVar.getType() + " but cannot be!");
            }
        }

        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assn : variables) {
            out.addAll(assn.getRequiredWfRunVarNames());
        }
        return out;
    }

    public List<VarNameAndValModel> assignInputVars(ThreadRunModel thread) throws LHVarSubError {
        List<VarNameAndValModel> out = new ArrayList<>();
        if (getTaskDef().getInputVars().size() != variables.size()) {
            throw new LHVarSubError(null, "Impossible: got different number of taskdef vars and node input vars");
        }

        for (int i = 0; i < taskDef.inputVars.size(); i++) {
            VariableDefModel requiredVarDef = taskDef.inputVars.get(i);
            VariableAssignmentModel assn = variables.get(i);
            String varName = requiredVarDef.getName();
            VariableValueModel val;

            if (assn != null) {
                val = thread.assignVariable(assn);
            } else {
                throw new LHVarSubError(null, "Variable " + varName + " is unassigned.");
            }
            if (val.getType() != requiredVarDef.getType() && val.getType() != null) {
                throw new LHVarSubError(
                        null,
                        "Variable " + varName + " should be " + requiredVarDef.getType() + " but is of type "
                                + val.getType());
            }
            out.add(new VarNameAndValModel(varName, val));
        }

        return out;
    }

    @Override
    public TaskNodeRunModel createSubNodeRun(Date time) {
        TaskNodeRunModel out = new TaskNodeRunModel(processorContext);
        // Note: all of the initialization is done in `TaskNodeRun#arrive()`
        return out;
    }
}
