package io.littlehorse.common.model.event;

import io.littlehorse.common.proto.scheduler.TaskScheduleRequestPb;
import io.littlehorse.common.proto.scheduler.TaskScheduleRequestPbOrBuilder;

public class TaskScheduleRequest {
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

    public TaskScheduleRequestPb.Builder toProtoBuilder() {
        return TaskScheduleRequestPb.newBuilder()
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

    public static TaskScheduleRequest fromProto(TaskScheduleRequestPbOrBuilder p) {
        TaskScheduleRequest out = new TaskScheduleRequest();
        out.taskDefId = p.getTaskDefId();
        out.taskDefName = p.getTaskDefName();
        out.threadRunNumber = p.getThreadRunNumber();
        out.taskRunNumber = p.getTaskRunNumber();
        out.taskRunPosition = p.getTaskRunPosition();
        out.wfRunId = p.getWfRunId();
        out.replyKafkaTopic = p.getReplyKafkaTopic();
        out.wfSpecId = p.getWfSpecId();
        out.attemptNumber = p.getAttemptNumber();
        out.nodeName = p.getNodeName();
        return out;
    }
}
