package io.littlehorse.common.model.event;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TaskStartedEventPb;
import io.littlehorse.common.proto.TaskStartedEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class TaskStartedEvent
    extends LHSerializable<TaskStartedEventPb>
    implements WfRunSubEvent {

    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;

    public Class<TaskStartedEventPb> getProtoBaseClass() {
        return TaskStartedEventPb.class;
    }

    public TaskStartedEventPb.Builder toProto() {
        TaskStartedEventPb.Builder b = TaskStartedEventPb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time));
        return b;
    }

    public static TaskStartedEvent fromProto(TaskStartedEventPbOrBuilder proto) {
        TaskStartedEvent out = new TaskStartedEvent();
        out.initFrom(proto);
        return out;
    }

    public void initFrom(MessageOrBuilder p) {
        TaskStartedEventPbOrBuilder proto = (TaskStartedEventPbOrBuilder) p;
        this.threadRunNumber = proto.getThreadRunNumber();
        this.taskRunNumber = proto.getTaskRunNumber();
        this.taskRunPosition = proto.getTaskRunPosition();
        this.time = LHUtil.fromProtoTs(proto.getTime());
    }

    public Integer getThreadRunNumber() {
        return threadRunNumber;
    }

    public Integer getNodeRunPosition() {
        return taskRunPosition;
    }
}
