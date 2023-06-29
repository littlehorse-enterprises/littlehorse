package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.wfrun.subnoderun.TaskRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.TaskNodePb;
import io.littlehorse.jlib.common.proto.VariableAssignmentPb;
import io.littlehorse.jlib.common.proto.VariableAssignmentPb.SourceCase;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskNode extends SubNode<TaskNodePb> {

    public String taskDefName;
    public int retries;
    public List<VariableAssignment> variables;
    public VariableAssignment timeoutSeconds;

    private TaskDef taskDef;

    public TaskDef getTaskDef(LHDAO dao) {
        if (taskDef == null) {
            taskDef = dao.getTaskDef(taskDefName);
        }
        return taskDef;
    }

    private Node node;

    public TaskNode() {
        variables = new ArrayList<>();
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Class<TaskNodePb> getProtoBaseClass() {
        return TaskNodePb.class;
    }

    public void initFrom(Message proto) {
        TaskNodePb p = (TaskNodePb) proto;
        taskDefName = p.getTaskDefName();
        retries = p.getRetries();

        if (p.hasTimeoutSeconds()) {
            timeoutSeconds = VariableAssignment.fromProto(p.getTimeoutSeconds());
        }

        for (VariableAssignmentPb assn : p.getVariablesList()) {
            variables.add(VariableAssignment.fromProto(assn));
        }
    }

    public TaskNodePb.Builder toProto() {
        TaskNodePb.Builder out = TaskNodePb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setRetries(retries);

        for (VariableAssignment va : variables) {
            out.addVariables(va.toProto());
        }

        if (timeoutSeconds != null) {
            out.setTimeoutSeconds(timeoutSeconds.toProto());
        }
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // Want to be able to release new versions of taskdef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the taskdef to ensure that this isn't an issue.
        TaskDef taskDef = stores.getTaskDef(taskDefName);
        if (taskDef == null) {
            throw new LHValidationError(
                null,
                "Refers to nonexistent TaskDef " + taskDefName
            );
        }
        if (retries < 0) {
            throw new LHValidationError(null, "has negative " + "number of retries!");
        }

        // Now need to validate that all of the variables are provided.
        if (variables.size() != taskDef.inputVars.size()) {
            throw new LHValidationError(
                null,
                "For TaskDef " +
                taskDef.name +
                " we need " +
                taskDef.inputVars.size() +
                " input vars, but we have " +
                variables.size()
            );
        }

        // EMPLOYEE_TODO: do some checking of types so that users can't shoot
        // themselves in the foot with mismatched var types. As part of this large
        // project, we will have to add schemas for JSON typed variables, and do
        // some jsonpath processing as well.

        if (timeoutSeconds == null) {
            timeoutSeconds = config.getDefaultTaskTimeout();
        } else {
            node.threadSpec.validateTimeoutAssignment(node.name, timeoutSeconds);
        }
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        if (
            timeoutSeconds != null &&
            timeoutSeconds.getRhsSourceType() == SourceCase.VARIABLE_NAME
        ) {
            out.add(timeoutSeconds.getVariableName());
        }

        for (VariableAssignment assn : variables) {
            out.addAll(assn.getRequiredWfRunVarNames());
        }
        return out;
    }

    public TaskRun createRun(Date time) {
        TaskRun out = new TaskRun();
        out.taskDefName = taskDefName;

        return out;
    }
}
