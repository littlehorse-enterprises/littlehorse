package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TaskScheduledOePb;
import io.littlehorse.common.proto.VariableValuePb;
import java.util.HashMap;
import java.util.Map;

public class TaskScheduledOe extends LHSerializable<TaskScheduledOePb> {

    public String taskDefId;
    public int taskRunAttemptNumber;
    public Map<String, VariableValue> variables;

    public Class<TaskScheduledOePb> getProtoBaseClass() {
        return TaskScheduledOePb.class;
    }

    public TaskScheduledOePb.Builder toProto() {
        TaskScheduledOePb.Builder out = TaskScheduledOePb
            .newBuilder()
            .setTaskDefId(taskDefId)
            .setTaskRunAttemptNumber(taskRunAttemptNumber);

        for (Map.Entry<String, VariableValue> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }

        return out;
    }

    public TaskScheduledOe(TaskScheduleRequest tsr) {
        variables = tsr.variables; // Note: don't mutate this.
        taskDefId = tsr.taskDefId;
        taskRunAttemptNumber = tsr.attemptNumber;
    }

    public TaskScheduledOe() {
        variables = new HashMap<>();
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskScheduledOePb p = (TaskScheduledOePb) proto;
        taskDefId = p.getTaskDefId();
        taskRunAttemptNumber = p.getTaskRunAttemptNumber();

        for (Map.Entry<String, VariableValuePb> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValue.fromProto(e.getValue()));
        }
    }

    public static TaskScheduledOe fromProto(TaskScheduledOePb proto) {
        TaskScheduledOe out = new TaskScheduledOe();
        out.initFrom(proto);
        return out;
    }
}
