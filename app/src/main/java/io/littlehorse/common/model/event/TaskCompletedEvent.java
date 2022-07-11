package io.littlehorse.common.model.event;

import java.util.Date;
import com.google.protobuf.ByteString;
import io.littlehorse.common.LHUtil;
import io.littlehorse.common.proto.TaskCompletedEventPb;
import io.littlehorse.common.proto.TaskCompletedEventPbOrBuilder;

public class TaskCompletedEvent {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;
    public byte[] stdout;
    public byte[] stderr;
    public boolean success;

    public TaskCompletedEventPb.Builder toProtoBuilder() {
        TaskCompletedEventPb.Builder b = TaskCompletedEventPb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time))
            .setSuccess(success);
        
        if (stdout != null) {
            b.setOutput(ByteString.copyFrom(stdout));
        }
        if (stderr != null) {
            b.setLogOutput(ByteString.copyFrom(stderr));
        }
        return b;
    }

    public static TaskCompletedEvent fromProto(TaskCompletedEventPbOrBuilder proto) {
        TaskCompletedEvent out = new TaskCompletedEvent();
        out.threadRunNumber = proto.getThreadRunNumber();
        out.taskRunNumber = proto.getTaskRunNumber();
        out.taskRunPosition = proto.getTaskRunPosition();
        out.time = LHUtil.fromProtoTs(proto.getTime());
        out.success = proto.getSuccess();

        if (proto.hasOutput()) {
            out.stdout = proto.getOutput().toByteArray();
        }
        if (proto.hasLogOutput()) {
            out.stderr = proto.getLogOutput().toByteArray();
        }

        return out;
    }

}
