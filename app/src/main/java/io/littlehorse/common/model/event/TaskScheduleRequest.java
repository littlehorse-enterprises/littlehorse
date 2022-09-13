package io.littlehorse.common.model.event;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.TaskScheduleRequestPb;
import io.littlehorse.common.proto.TaskScheduleRequestPbOrBuilder;

public class TaskScheduleRequest extends LHSerializable<TaskScheduleRequestPb> {

    public String taskDefId;
    public String taskDefName;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public String wfRunId;
    public String replyKafkaTopic;
    public String wfSpecId;
    public int attemptNumber;
    public String nodeName;

    public TaskScheduleRequestPb.Builder toProto() {
        return TaskScheduleRequestPb
            .newBuilder()
            .setTaskDefId(taskDefId)
            .setTaskDefName(taskDefName)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setWfRunId(wfRunId)
            .setReplyKafkaTopic(replyKafkaTopic)
            .setWfSpecId(wfSpecId)
            .setAttemptNumber(attemptNumber)
            .setNodeName(nodeName);
    }

    public Class<TaskScheduleRequestPb> getProtoBaseClass() {
        return TaskScheduleRequestPb.class;
    }

    public static TaskScheduleRequest fromProto(
        TaskScheduleRequestPbOrBuilder p
    ) {
        TaskScheduleRequest out = new TaskScheduleRequest();
        out.initFrom(p);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskScheduleRequestPbOrBuilder p = (TaskScheduleRequestPbOrBuilder) proto;
        this.taskDefId = p.getTaskDefId();
        this.taskDefName = p.getTaskDefName();
        this.threadRunNumber = p.getThreadRunNumber();
        this.taskRunNumber = p.getTaskRunNumber();
        this.taskRunPosition = p.getTaskRunPosition();
        this.wfRunId = p.getWfRunId();
        this.replyKafkaTopic = p.getReplyKafkaTopic();
        this.wfSpecId = p.getWfSpecId();
        this.attemptNumber = p.getAttemptNumber();
        this.nodeName = p.getNodeName();
    }
}
