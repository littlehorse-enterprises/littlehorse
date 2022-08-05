package io.littlehorse.common.model.observability;

import com.google.protobuf.ByteString;
import io.littlehorse.common.model.event.TaskCompletedEvent;
import io.littlehorse.common.proto.observability.TaskCompleteOePb;

public class TaskCompleteOe {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;

    public boolean success;
    public byte[] output;
    public byte[] logOutput;

    public String nodeName;

    public TaskCompleteOePb.Builder toProtoBuilder() {
        TaskCompleteOePb.Builder out = TaskCompleteOePb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setSuccess(success)
            .setNodeName(nodeName);

        if (output != null) {
            out.setOutput(ByteString.copyFrom(output));
        }
        if (logOutput != null) {
            out.setLogOutput(ByteString.copyFrom(logOutput));
        }

        return out;
    }

    public TaskCompleteOe(TaskCompletedEvent evt, String nodeName) {
        this.nodeName = nodeName;
    
        threadRunNumber = evt.threadRunNumber;
        taskRunNumber = evt.taskRunNumber;
        taskRunPosition = evt.taskRunPosition;

        success = evt.success;
        output = evt.stdout;
        logOutput = evt.stderr;
    }
}
