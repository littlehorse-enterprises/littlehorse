package io.littlehorse.common.model.observability;

import com.google.protobuf.ByteString;
import io.littlehorse.common.proto.TaskCompleteOePb;

public class TaskCompleteOe {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;

    public boolean success;
    public byte[] output;
    public byte[] logOutput;


    public TaskCompleteOePb.Builder toProtoBuilder() {
        TaskCompleteOePb.Builder out = TaskCompleteOePb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setSuccess(success);

        if (output != null) {
            out.setOutput(ByteString.copyFrom(output));
        }
        if (logOutput != null) {
            out.setLogOutput(ByteString.copyFrom(logOutput));
        }

        return out;
    }
}
