package io.littlehorse.common.model.observability;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskCompletedEvent;
import io.littlehorse.common.proto.observability.TaskCompleteOePb;

public class TaskCompleteOe extends LHSerializable<TaskCompleteOePb> {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;

    public boolean success;
    public byte[] output;
    public byte[] logOutput;

    public String nodeName;

    public Class<TaskCompleteOePb> getProtoBaseClass() {
        return TaskCompleteOePb.class;
    }

    public TaskCompleteOePb.Builder toProto() {
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

    public void initFrom(MessageOrBuilder proto) {
        TaskCompleteOePb p = (TaskCompleteOePb) proto;
        threadRunNumber = p.getThreadRunNumber();
        taskRunNumber = p.getTaskRunNumber();
        taskRunPosition = p.getTaskRunPosition();
        success = p.getSuccess();
        if (p.hasOutput()) output = p.getOutput().toByteArray();
        if (p.hasLogOutput()) logOutput = p.getLogOutput().toByteArray();
        nodeName = p.getNodeName();
    }

    public TaskCompleteOe() {}

    public TaskCompleteOe(TaskCompletedEvent evt, String nodeName) {
        this.nodeName = nodeName;
    
        threadRunNumber = evt.threadRunNumber;
        taskRunNumber = evt.taskRunNumber;
        taskRunPosition = evt.taskRunPosition;

        success = evt.success;
        output = evt.stdout;
        logOutput = evt.stderr;
    }

    public static TaskCompleteOe fromProto(TaskCompleteOePb proto) {
        TaskCompleteOe out = new TaskCompleteOe();
        out.initFrom(proto);
        return out;
    }
}
