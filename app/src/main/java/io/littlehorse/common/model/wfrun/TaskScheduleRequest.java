package io.littlehorse.common.model.wfrun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TaskScheduleRequestPb;
import io.littlehorse.common.proto.TaskScheduleRequestPbOrBuilder;
import io.littlehorse.common.proto.VariableValuePb;
import java.util.HashMap;
import java.util.Map;

public class TaskScheduleRequest extends LHSerializable<TaskScheduleRequestPb> {

    public String taskDefId;
    public String taskDefName;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public String wfRunId;
    public String wfRunEventQueue;
    public String wfSpecId;
    public int attemptNumber;
    public String nodeName;
    public Map<String, VariableValue> variables;

    public TaskScheduleRequest() {
        variables = new HashMap<>();
    }

    public TaskScheduleRequestPb.Builder toProto() {
        TaskScheduleRequestPb.Builder out = TaskScheduleRequestPb
            .newBuilder()
            .setTaskDefId(taskDefId)
            .setTaskDefName(taskDefName)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setWfRunId(wfRunId)
            .setWfRunEventQueue(wfRunEventQueue)
            .setWfSpecId(wfSpecId)
            .setAttemptNumber(attemptNumber)
            .setNodeName(nodeName);

        for (Map.Entry<String, VariableValue> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }

        return out;
    }

    public Class<TaskScheduleRequestPb> getProtoBaseClass() {
        return TaskScheduleRequestPb.class;
    }

    public static TaskScheduleRequest fromProto(TaskScheduleRequestPbOrBuilder p) {
        TaskScheduleRequest out = new TaskScheduleRequest();
        out.initFrom(p);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskScheduleRequestPbOrBuilder p = (TaskScheduleRequestPbOrBuilder) proto;
        this.taskDefId = p.getTaskDefId();
        this.taskDefName = p.getTaskDefName();
        this.threadRunNumber = p.getThreadRunNumber();
        this.taskRunNumber = p.getTaskRunNumber();
        this.taskRunPosition = p.getTaskRunPosition();
        this.wfRunId = p.getWfRunId();
        this.wfRunEventQueue = p.getWfRunEventQueue();
        this.wfSpecId = p.getWfSpecId();
        this.attemptNumber = p.getAttemptNumber();
        this.nodeName = p.getNodeName();

        for (Map.Entry<String, VariableValuePb> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValue.fromProto(e.getValue()));
        }
    }
}
