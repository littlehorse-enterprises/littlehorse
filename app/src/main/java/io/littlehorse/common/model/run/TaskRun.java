package io.littlehorse.common.model.run;

import java.util.Date;
import com.google.protobuf.ByteString;
import io.littlehorse.common.LHUtil;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskRunPb;
import io.littlehorse.common.proto.TaskRunPbOrBuilder;

public class TaskRun {
    public String wfRunId;
    public int threadRunNumber;
    public int orderPosition;
    public int number;

    public String nodeName;
    public String nodeId;

    public String taskDefId;
    public String workerId;
    public int attemptNumber;

    public LHStatusPb status;

    public byte[] stdout;
    public byte[] stderr;
    public int returnCode;

    public Date scheduledTime;
    public Date startedTime;
    public Date endedTime;

    public TaskRunPb.Builder toProtoBuilder() {
        TaskRunPb.Builder b = TaskRunPb.newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setOrderPosition(orderPosition)
            .setNumber(number)
            .setNodeName(nodeName)
            .setNodeId(nodeId)
            .setTaskDefId(taskDefId)
            .setAttemptNumber(attemptNumber)
            .setStatus(status)
            .setReturnCode(returnCode);

        if (workerId != null) b.setWorkerId(workerId);
        if (scheduledTime != null) b.setScheduledTime(LHUtil.fromDate(scheduledTime));
        if (startedTime != null) b.setStartedTime(LHUtil.fromDate(startedTime));
        if (endedTime != null) b.setEndedTime(LHUtil.fromDate(endedTime));
        if (stdout != null) b.setStdout(ByteString.copyFrom(stdout));
        if (stderr != null) b.setStderr(ByteString.copyFrom(stderr));

        return b;
    }

    public static TaskRun fromProto(TaskRunPbOrBuilder proto) {
        TaskRun out = new TaskRun();
        out.wfRunId = proto.getWfRunId();
        out.threadRunNumber = proto.getThreadRunNumber();
        out.orderPosition = proto.getOrderPosition();
        out.number = proto.getNumber();
        out.nodeName = proto.getNodeName();
        out.nodeId = proto.getNodeId();
        out.taskDefId = proto.getTaskDefId();
        out.workerId = proto.hasWorkerId() ? proto.getWorkerId() : null;
        out.attemptNumber = proto.getAttemptNumber();
        out.status = proto.getStatus();

        if (proto.hasStdout()) {
            out.stdout = proto.getStdout().toByteArray();
        }
        if (proto.hasStderr()) {
            out.stderr = proto.getStderr().toByteArray();
        }
        out.returnCode = proto.getReturnCode();
        if (proto.hasScheduledTime()) {
            out.scheduledTime = LHUtil.fromProtoTs(proto.getScheduledTime());
        }
        if (proto.hasStartedTime()) {
            out.startedTime = LHUtil.fromProtoTs(proto.getStartedTime());
        }
        if (proto.hasEndedTime()) {
            out.endedTime = LHUtil.fromProtoTs(proto.getEndedTime());
        }
        return out;
    }
}
