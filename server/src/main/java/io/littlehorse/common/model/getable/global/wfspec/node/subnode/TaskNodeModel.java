package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.TaskNodeRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.TaskNode;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableType;
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

    public String taskDefName;
    public int retries;
    public List<VariableAssignmentModel> variables;
    public int timeoutSeconds;

    private TaskDefModel taskDef;
    private CoreProcessorDAO dao;

    public TaskDefModel getTaskDef() {
        if (taskDef == null) {
            if (dao == null && node != null) {
                // Only works for when this is part of a Node, not a UTATask.
                dao = node.getThreadSpecModel().getWfSpecModel().getDao();
            }
            taskDef = dao.getTaskDef(taskDefName);
        }
        return taskDef;
    }

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

    public void initFrom(Message proto) {
        TaskNode p = (TaskNode) proto;
        taskDefName = p.getTaskDefName();
        retries = p.getRetries();

        timeoutSeconds = p.getTimeoutSeconds();
        if (timeoutSeconds == 0) {
            timeoutSeconds = LHConstants.DEFAULT_TASK_TIMEOUT_SECONDS;
        }

        for (VariableAssignment assn : p.getVariablesList()) {
            variables.add(VariableAssignmentModel.fromProto(assn));
        }
    }

    public TaskNode.Builder toProto() {
        TaskNode.Builder out = TaskNode.newBuilder()
                .setTaskDefName(taskDefName)
                .setTimeoutSeconds(timeoutSeconds)
                .setRetries(retries);

        for (VariableAssignmentModel va : variables) {
            out.addVariables(va.toProto());
        }
        return out;
    }

    public void validate(ReadOnlyMetadataStore stores, LHServerConfig config) throws LHApiException {
        // Want to be able to release new versions of taskdef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the taskdef to ensure that this isn't an issue.
        TaskDefModel taskDef = stores.getTaskDef(taskDefName);
        if (taskDef == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Refers to nonexistent TaskDef " + taskDefName);
        }
        if (retries < 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "has negative " + "number of retries!");
        }

        // Now need to validate that all of the variables are provided.
        if (variables.size() != taskDef.inputVars.size()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "For TaskDef "
                            + taskDef.name
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
            if (!assn.canBeType(taskDefVar.getType(), this.node.getThreadSpecModel())) {
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
            String varName = requiredVarDef.name;
            VariableValueModel val;

            if (assn != null) {
                val = thread.assignVariable(assn);
            } else {
                throw new LHVarSubError(null, "Variable " + varName + " is unassigned.");
            }
            if (val.type != requiredVarDef.type && val.type != VariableType.NULL) {
                throw new LHVarSubError(
                        null,
                        "Variable " + varName + " should be " + requiredVarDef.type + " but is of type " + val.type);
            }
            out.add(new VarNameAndValModel(varName, val));
        }

        return out;
    }

    @Override
    public TaskNodeRunModel createSubNodeRun(Date time) {
        TaskNodeRunModel out = new TaskNodeRunModel();
        // Note: all of the initialization is done in `TaskNodeRun#arrive()`
        return out;
    }
}
