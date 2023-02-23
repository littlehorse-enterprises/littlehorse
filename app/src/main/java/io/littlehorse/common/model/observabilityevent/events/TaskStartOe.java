package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.TaskStartOePb;
import io.littlehorse.jlib.common.proto.TaskStartOePbOrBuilder;

public class TaskStartOe extends SubEvent<TaskStartOePb> {

    public String workerId;
    public int threadRunNumber;
    public int taskRunPosition;

    public Class<TaskStartOePb> getProtoBaseClass() {
        return TaskStartOePb.class;
    }

    public TaskStartOePb.Builder toProto() {
        TaskStartOePb.Builder out = TaskStartOePb
            .newBuilder()
            .setWorkerId(workerId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunPosition(taskRunPosition);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskStartOePbOrBuilder p = (TaskStartOePbOrBuilder) proto;
        taskRunPosition = p.getTaskRunPosition();
        threadRunNumber = p.getThreadRunNumber();
        workerId = p.getWorkerId();
    }
}
