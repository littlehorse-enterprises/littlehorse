package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.proto.observability.TaskScheduledOePb;

public class TaskScheduledOe extends LHSerializable<TaskScheduledOePb> {
    public String taskDefId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public int taskRunAttemptNumber;
    public String wfRunId;
    public String nodeName;

    public Class<TaskScheduledOePb> getProtoBaseClass() {
        return TaskScheduledOePb.class;
    }

    public TaskScheduledOePb.Builder toProto() {
        TaskScheduledOePb.Builder out = TaskScheduledOePb.newBuilder()
            .setTaskDefId(taskDefId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTaskRunAttemptNumber(taskRunAttemptNumber)
            .setWfRunId(wfRunId)
            .setNodeName(nodeName);

        return out;
    }

    public TaskScheduledOe(TaskScheduleRequest tsr) {
        taskDefId = tsr.taskDefId;
        threadRunNumber = tsr.threadRunNumber;
        taskRunNumber = tsr.taskRunNumber;
        taskRunPosition = tsr.taskRunPosition;
        taskRunAttemptNumber = tsr.attemptNumber;
        wfRunId = tsr.wfRunId;
        nodeName = tsr.nodeName;
    }

    public TaskScheduledOe() {}

    public void initFrom(MessageOrBuilder proto) {
        TaskScheduledOePb p = (TaskScheduledOePb) proto;
        taskDefId = p.getTaskDefId();
        threadRunNumber = p.getThreadRunNumber();
        taskRunNumber = p.getTaskRunNumber();
        taskRunPosition = p.getTaskRunPosition();
        taskRunAttemptNumber = p.getTaskRunAttemptNumber();
        wfRunId = p.getWfRunId();
        nodeName = p.getNodeName();
    }

    public static TaskScheduledOe fromProto(TaskScheduledOePb proto) {
        TaskScheduledOe out = new TaskScheduledOe();
        out.initFrom(proto);
        return out;
    }
}
