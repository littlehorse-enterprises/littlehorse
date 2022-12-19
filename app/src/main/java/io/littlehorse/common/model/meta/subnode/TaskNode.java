package io.littlehorse.common.model.meta.subnode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.wfrun.subnoderun.TaskRun;
import io.littlehorse.common.proto.TaskNodePb;
import io.littlehorse.common.proto.TaskNodePbOrBuilder;
import io.littlehorse.common.proto.VariableAssignmentPb;
import io.littlehorse.common.proto.VariableAssignmentPb.SourceCase;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TaskNode extends SubNode<TaskNodePb> {

    public String taskDefName;
    public int retries;
    public Map<String, VariableAssignment> variables;
    public VariableAssignment timeoutSeconds;

    @JsonIgnore
    private TaskDef taskDef;

    @JsonIgnore
    public TaskDef getTaskDef(LHDAO dao) {
        if (taskDef == null) {
            taskDef = dao.getTaskDef(taskDefName, null);
        }
        return taskDef;
    }

    @JsonIgnore
    private Node node;

    public TaskNode() {
        variables = new HashMap<>();
    }

    @JsonIgnore
    public void setNode(Node node) {
        this.node = node;
    }

    public Class<TaskNodePb> getProtoBaseClass() {
        return TaskNodePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskNodePbOrBuilder p = (TaskNodePbOrBuilder) proto;
        taskDefName = p.getTaskDefName();
        retries = p.getRetries();

        if (p.hasTimeoutSeconds()) {
            timeoutSeconds = VariableAssignment.fromProto(p.getTimeoutSeconds());
        }

        for (Map.Entry<String, VariableAssignmentPb> entry : p
            .getVariablesMap()
            .entrySet()) {
            variables.put(
                entry.getKey(),
                VariableAssignment.fromProto(entry.getValue())
            );
        }
    }

    public TaskNodePb.Builder toProto() {
        TaskNodePb.Builder out = TaskNodePb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setRetries(retries);

        for (Map.Entry<String, VariableAssignment> entry : variables.entrySet()) {
            out.putVariables(entry.getKey(), entry.getValue().toProto().build());
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
        TaskDef taskDef = stores.getTaskDef(taskDefName, null);
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
        for (Map.Entry<String, VariableDef> e : taskDef.inputVars.entrySet()) {
            VariableDef varDef = e.getValue();
            if (varDef.defaultValue == null) {
                // Then we NEED the value.
                if (!variables.containsKey(e.getKey())) {
                    throw new LHValidationError(
                        null,
                        "Missing required input variable " + e.getKey()
                    );
                }
            }
            // TODO: May want to do some validation of types.
        }

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
            timeoutSeconds.rhsSourceType == SourceCase.VARIABLE_NAME
        ) {
            out.add(timeoutSeconds.rhsVariableName);
        }

        for (Map.Entry<String, VariableAssignment> e : variables.entrySet()) {
            out.addAll(e.getValue().getRequiredVariableNames());
        }
        return out;
    }

    public TaskRun createRun(Date time) {
        TaskRun out = new TaskRun();
        out.taskDefName = taskDefName;

        return out;
    }
}
