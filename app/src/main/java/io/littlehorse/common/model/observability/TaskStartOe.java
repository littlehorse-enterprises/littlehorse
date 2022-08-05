package io.littlehorse.common.model.observability;

import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.proto.observability.TaskStartOePb;

public class TaskStartOe {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public String nodeName;

    public TaskStartOePb.Builder toProtoBuilder() {
        TaskStartOePb.Builder out = TaskStartOePb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setNodeName(nodeName);

        return out;
    }

    public TaskStartOe(TaskStartedEvent se, String nodename) {
        threadRunNumber = se.threadRunNumber;
        taskRunNumber = se.taskRunNumber;
        taskRunPosition = se.taskRunPosition;
        this.nodeName = nodename;
    }
}
