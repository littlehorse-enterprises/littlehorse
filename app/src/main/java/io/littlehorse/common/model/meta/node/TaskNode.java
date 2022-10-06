package io.littlehorse.common.model.meta.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.proto.TaskNodePb;
import io.littlehorse.common.proto.TaskNodePbOrBuilder;
import io.littlehorse.common.proto.VariableAssignmentPb;
import java.util.HashMap;
import java.util.Map;

public class TaskNode extends LHSerializable<TaskNodePb> {

    public String taskDefName;
    public VariableAssignment timeoutSeconds;
    public int retries;
    public Map<String, VariableAssignment> variables;

    @JsonIgnore
    public TaskDef taskDef;

    public TaskNode() {
        variables = new HashMap<>();
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

        if (timeoutSeconds != null) {
            out.setTimeoutSeconds(timeoutSeconds.toProto());
        }
        for (Map.Entry<String, VariableAssignment> entry : variables.entrySet()) {
            out.putVariables(entry.getKey(), entry.getValue().toProto().build());
        }
        return out;
    }
}
