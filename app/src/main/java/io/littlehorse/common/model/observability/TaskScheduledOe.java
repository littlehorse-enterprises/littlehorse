package io.littlehorse.common.model.observability;

import io.littlehorse.common.proto.TaskScheduledOePb;

public class TaskScheduledOe {
    public String taskDefId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public int taskRunAttemptNumber;
    public String wfRunId;

    public TaskScheduledOePb.Builder toProtoBuilder() {
        TaskScheduledOePb.Builder out = TaskScheduledOePb.newBuilder()
            .setTaskDefId(taskDefId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTaskRunAttemptNumber(taskRunAttemptNumber)
            .setWfRunId(wfRunId);

        return out;
    }
}
