package io.littlehorse.server.model.wfrun;

import java.util.Date;
import com.google.protobuf.ByteString;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskRunPb;
import io.littlehorse.common.util.LHUtil;

public class TaskRun extends GETable<TaskRunPb> {
    public String wfRunId;
    public int threadRunNumber;
    public int position;

    public int number;
    public int attemptNumber;
    public LHStatusPb status;
    public byte[] output;
    public byte[] logOutput;

    public Date scheduleTime;
    public Date startTime;
    public Date endTime;

    public String wfSpecId;
    public String threadSpecName;
    public String nodeName;
    public String taskDefId;

    public String getStoreKey() {
        return wfRunId + "-" + threadRunNumber + "-" + position;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<TaskRunPb> getProtoBaseClass() {
        return TaskRunPb.class;
    }

    public void initFrom(TaskRunPb proto) {
        wfRunId = proto.getWfRunId();
        threadRunNumber = proto.getThreadRunNumber();
        position = proto.getPosition();

        number = proto.getNumber();
        attemptNumber = proto.getAttemptNumber();
        if (proto.hasOutput()) output = proto.getOutput().toByteArray();
        if (proto.hasLogOutput()) output = proto.getLogOutput().toByteArray();

        scheduleTime = LHUtil.fromProtoTs(proto.getScheduleTime());
        if (proto.hasStartTime()) {
            startTime = LHUtil.fromProtoTs(proto.getStartTime());
        }
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        wfSpecId = proto.getWfSpecId();
        threadSpecName = proto.getThreadSpecName();
        nodeName = proto.getNodeName();
        taskDefId = proto.getTaskDefId();
    }

    public TaskRunPb.Builder toProto() {
        TaskRunPb.Builder out = TaskRunPb.newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setPosition(position)
            .setNumber(number)
            .setAttemptNumber(attemptNumber)
            .setStatus(status)
            .setScheduleTime(LHUtil.fromDate(scheduleTime))
            .setWfSpecId(wfSpecId)
            .setNodeName(nodeName)
            .setTaskDefId(taskDefId);

        if (output != null) out.setOutput(ByteString.copyFrom(output));
        if (logOutput != null) out.setLogOutput(ByteString.copyFrom(logOutput));

        if (startTime != null) out.setStartTime(LHUtil.fromDate(startTime));
        if (endTime != null) out.setEndTime(LHUtil.fromDate(endTime));

        return out;
    }
}
