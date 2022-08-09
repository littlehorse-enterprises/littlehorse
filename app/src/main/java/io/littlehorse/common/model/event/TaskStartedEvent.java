package io.littlehorse.common.model.event;

import java.util.Date;
import io.littlehorse.common.proto.scheduler.TaskStartedEventPb;
import io.littlehorse.common.proto.scheduler.TaskStartedEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class TaskStartedEvent {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;

    public TaskStartedEventPb.Builder toProto() {
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
