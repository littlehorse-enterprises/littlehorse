package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.TaskAttemptPb;
import io.littlehorse.jlib.common.proto.TaskStatusPb;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAttempt extends LHSerializable<TaskAttemptPb> {

    private VariableValue output;
    private VariableValue logOutput;

    private Date scheduleTime;
    private Date startTime;
    private Date endTime;
    private String taskWorkerId;
    private String taskWorkerVersion;
    private TaskStatusPb status;

    public TaskAttempt() {
        scheduleTime = new Date();
        status = TaskStatusPb.TASK_SCHEDULED;
    }

    public Class<TaskAttemptPb> getProtoBaseClass() {
        return TaskAttemptPb.class;
    }

    public void initFrom(Message proto) {
        TaskAttemptPb p = (TaskAttemptPb) proto;
        if (p.hasOutput()) {
            output = VariableValue.fromProto(p.getOutput());
        }
        if (p.hasScheduleTime()) {
            scheduleTime = LHUtil.fromProtoTs(p.getScheduleTime());
        }
        if (p.hasLogOutput()) {
            logOutput = VariableValue.fromProto(p.getLogOutput());
        }
        if (p.hasStartTime()) {
            startTime = LHUtil.fromProtoTs(p.getStartTime());
        }
        if (p.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(p.getEndTime());
        }
        if (p.hasTaskWorkerVersion()) {
            taskWorkerVersion = p.getTaskWorkerVersion();
        }
        taskWorkerId = p.getTaskWorkerId();
        status = p.getStatus();
    }

    public TaskAttemptPb.Builder toProto() {
        TaskAttemptPb.Builder out = TaskAttemptPb.newBuilder();

        if (taskWorkerId != null) {
            out.setTaskWorkerId(taskWorkerId);
        }
        if (taskWorkerVersion != null) {
            out.setTaskWorkerVersion(taskWorkerVersion);
        }
        if (output != null) {
            out.setOutput(output.toProto());
        }
        if (logOutput != null) {
            out.setLogOutput(logOutput.toProto());
        }
        if (scheduleTime != null) {
            out.setScheduleTime(LHUtil.fromDate(scheduleTime));
        }
        if (startTime != null) {
            out.setStartTime(LHUtil.fromDate(startTime));
        }
        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        out.setStatus(status);

        return out;
    }
}
