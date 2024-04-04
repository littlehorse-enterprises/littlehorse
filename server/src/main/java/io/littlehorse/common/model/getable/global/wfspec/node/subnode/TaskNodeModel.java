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
import io.littlehorse.sdk.common.proto.TaskNode.TaskToExecuteCase;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
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

    private TaskToExecuteCase taskToExecuteType;
    private TaskDefIdModel taskDefId; // BE CAREFUL WHEN USING THIS
    private VariableAssignmentModel dynamicTask;

    private List<VariableAssignmentModel> variables;
    private int timeoutSeconds;

    // private TaskDefModel taskDef;
    private WfService wfService;

    private int simpleRetries;
    private ExponentialBackoffRetryPolicyModel exponentialBackoffRetryPolicy;

    private NodeModel node;

    public TaskNodeModel() {
        variables = new ArrayList<>();
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

        timeoutSeconds = p.getTimeoutSeconds();
        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }

        for (VariableAssignment assn : p.getVariablesList()) {
            variables.add(VariableAssignmentModel.fromProto(assn, context));
        }

        this.taskToExecuteType = p.getTaskToExecuteCase();
        switch (taskToExecuteType) {
            case TASK_DEF_ID:
                taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
                break;
            case DYNAMIC_TASK:
                dynamicTask = LHSerializable.fromProto(p.getDynamicTask(), VariableAssignmentModel.class, context);
                break;
            case TASKTOEXECUTE_NOT_SET:
                throw new LHApiException(Status.INVALID_ARGUMENT, "Task Node did not set taskdef");
        }

        simpleRetries = p.getRetries();
        if (p.hasExponentialBackoff()) {
            exponentialBackoffRetryPolicy = LHSerializable.fromProto(
                    p.getExponentialBackoff(), ExponentialBackoffRetryPolicyModel.class, context);
        }
    }

    public TaskNode.Builder toProto() {
        TaskNode.Builder out =
                TaskNode.newBuilder().setTimeoutSeconds(timeoutSeconds).setRetries(simpleRetries);

        for (VariableAssignmentModel va : variables) {
            out.addVariables(va.toProto());
        }
        switch (taskToExecuteType) {
            case TASK_DEF_ID:
                out.setTaskDefId(taskDefId.toProto());
                break;
            case DYNAMIC_TASK:
                out.setDynamicTask(dynamicTask.toProto());
                break;
            case TASKTOEXECUTE_NOT_SET:
                throw new LHApiException(Status.INVALID_ARGUMENT, "Task Node did not set taskdef");
        }

        if (exponentialBackoffRetryPolicy != null) {
            out.setExponentialBackoff(exponentialBackoffRetryPolicy.toProto());
        }
        return out;
    }

    @Override
    public void validate(MetadataCommandExecution ctx) throws LHApiException {
        // Can only validate the type of TaskDef if we know it ahead of time...
        if (taskToExecuteType == TaskToExecuteCase.TASK_DEF_ID) {
            TaskDefModel taskDef = ctx.metadataManager().get(new TaskDefIdModel(taskDefId.getName()));
            if (taskDef == null) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "Refers to nonexistent TaskDef " + taskDefId);
            }

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
        }

        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }

        validateRetryPolicy();
    }

    private void validateRetryPolicy() throws LHApiException {
        if (simpleRetries < 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Cannot have negative retries!");
        }
        if (exponentialBackoffRetryPolicy == null) return;
        if (exponentialBackoffRetryPolicy.getBaseIntervalMs() <= 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Exponential Backoff Base interval must be > 0!");
        }
        if (exponentialBackoffRetryPolicy.getMultiplier() < 1.0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Exponential Backoff Multiplier must be at least 1.0!");
        }
        if (exponentialBackoffRetryPolicy.getMaxDelayMs() < exponentialBackoffRetryPolicy.getBaseIntervalMs()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "Exponential Backoff max delay cannot be less than base delay");
        }
        return;
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableAssignmentModel assn : variables) {
            out.addAll(assn.getRequiredWfRunVarNames());
        }
        if (dynamicTask != null) {
            out.addAll(dynamicTask.getRequiredWfRunVarNames());
        }
        return out;
    }

    public TaskDefModel getTaskDef(ThreadRunModel thread, ExecutionContext executionContext) throws LHVarSubError {
        switch (taskToExecuteType) {
            case TASK_DEF_ID:
                return executionContext.metadataManager().get(taskDefId);
            case DYNAMIC_TASK:
                String taskDefName = thread.assignVariable(dynamicTask).asStr().getStrVal();
                TaskDefModel out = executionContext.metadataManager().get(new TaskDefIdModel(taskDefName));
                if (out == null) {
                    throw new LHVarSubError(null, "No TaskDef named %s!".formatted(taskDefName));
                }
                return out;
            case TASKTOEXECUTE_NOT_SET:
        }
        throw new LHApiException(Status.INVALID_ARGUMENT, "Node does not specify Task to execute");
    }

    public List<VarNameAndValModel> assignInputVars(ThreadRunModel thread, ProcessorExecutionContext processorContext)
            throws LHVarSubError {
        TaskDefModel taskDef = getTaskDef(thread, processorContext);

        List<VarNameAndValModel> out = new ArrayList<>();
        if (taskDef.getInputVars().size() != variables.size()) {
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
    public TaskNodeRunModel createSubNodeRun(Date time, ProcessorExecutionContext processorContext) {
        TaskNodeRunModel out = new TaskNodeRunModel(processorContext);
        // Note: all of the initialization is done in `TaskNodeRun#arrive()`
        return out;
    }
}
