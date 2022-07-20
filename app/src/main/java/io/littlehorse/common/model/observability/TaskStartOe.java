package io.littlehorse.common.model.observability;

import io.littlehorse.common.proto.TaskStartOePb;

public class TaskStartOe {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;

    public TaskStartOePb.Builder toProtoBuilder() {
        TaskStartOePb.Builder out = TaskStartOePb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition);

        return out;
    }
}
