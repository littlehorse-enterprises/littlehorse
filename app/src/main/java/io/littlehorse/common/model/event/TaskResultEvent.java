package io.littlehorse.common.model.event;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.TaskResultEventPb;
import io.littlehorse.common.proto.TaskResultEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class TaskResultEvent extends LHSerializable<TaskResultEventPb> {

    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;
    public byte[] stdout;
    public byte[] stderr;
    public TaskResultCodePb resultCode;

    public Class<TaskResultEventPb> getProtoBaseClass() {
        return TaskResultEventPb.class;
    }

    public TaskResultEventPb.Builder toProto() {
        TaskResultEventPb.Builder b = TaskResultEventPb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time))
            .setResultCode(resultCode);

        if (stdout != null) b.setOutput(ByteString.copyFrom(stdout));
        if (stderr != null) b.setLogOutput(ByteString.copyFrom(stderr));

        return b;
    }

    public void initFrom(MessageOrBuilder p) {
        TaskResultEventPbOrBuilder proto = (TaskResultEventPbOrBuilder) p;
        this.threadRunNumber = proto.getThreadRunNumber();
        this.taskRunNumber = proto.getTaskRunNumber();
        this.taskRunPosition = proto.getTaskRunPosition();
        this.time = LHUtil.fromProtoTs(proto.getTime());
        this.resultCode = proto.getResultCode();

        if (proto.hasOutput()) {
            this.stdout = proto.getOutput().toByteArray();
        }
        if (proto.hasLogOutput()) {
            this.stderr = proto.getLogOutput().toByteArray();
        }
    }

    public static TaskResultEvent fromProto(TaskResultEventPbOrBuilder proto) {
        TaskResultEvent out = new TaskResultEvent();
        out.initFrom(proto);
        return out;
    }
}
