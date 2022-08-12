package io.littlehorse.scheduler.model;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.scheduler.TaskTimeoutPb;
import io.littlehorse.common.proto.scheduler.TaskTimeoutPbOrBuilder;

public class TaskTimeout extends LHSerializable<TaskTimeoutPb> {
    public String wfRunId;
    public int threadRunNumber;
    public int taskRunPosition;

    public void initFrom(MessageOrBuilder proto) {
        TaskTimeoutPbOrBuilder p = (TaskTimeoutPbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        taskRunPosition = p.getTaskRunPosition();
    }

    public TaskTimeoutPb.Builder toProto() {
        TaskTimeoutPb.Builder out = TaskTimeoutPb.newBuilder()
            .setWfRunId(wfRunId)
            .setTaskRunPosition(taskRunPosition)
            .setThreadRunNumber(threadRunNumber);

        return out;
    }

    public Class<TaskTimeoutPb> getProtoBaseClass() {
        return TaskTimeoutPb.class;
    }
}
