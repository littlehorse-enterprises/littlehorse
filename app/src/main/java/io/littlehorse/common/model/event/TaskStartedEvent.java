package io.littlehorse.common.model.event;

import java.util.Date;
import io.littlehorse.common.LHUtil;
import io.littlehorse.common.proto.TaskStartedEventPb;
import io.littlehorse.common.proto.TaskStartedEventPbOrBuilder;

public class TaskStartedEvent {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;

    public TaskStartedEventPb.Builder toProtoBuilder() {
        TaskStartedEventPb.Builder b = TaskStartedEventPb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time));
        return b;
    }

    public static TaskStartedEvent fromProto(TaskStartedEventPbOrBuilder proto) {
        TaskStartedEvent out = new TaskStartedEvent();
        out.threadRunNumber = proto.getThreadRunNumber();
        out.taskRunNumber = proto.getTaskRunNumber();
        out.taskRunPosition = proto.getTaskRunPosition();
        out.time = LHUtil.fromProtoTs(proto.getTime());

        return out;
    }
}
