package io.littlehorse.common.model.observability;

import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.proto.TaskScheduledOePb;

public class TaskScheduledOe {
    public String taskDefId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public int taskRunAttemptNumber;
    public String wfRunId;
    public String nodeName;

    public TaskScheduledOePb.Builder toProtoBuilder() {
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
}
