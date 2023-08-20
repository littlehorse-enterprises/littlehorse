package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskStatus;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAttemptModel extends LHSerializable<TaskAttempt> {

    private VariableValueModel output;
    private VariableValueModel logOutput;

    private Date scheduleTime;
    private Date startTime;
    private Date endTime;
    private String taskWorkerId;
    private String taskWorkerVersion;
    private TaskStatus status;

    public TaskAttemptModel() {
        scheduleTime = new Date();
        status = TaskStatus.TASK_SCHEDULED;
    }

    public Class<TaskAttempt> getProtoBaseClass() {
        return TaskAttempt.class;
    }

    public void initFrom(Message proto) {
        TaskAttempt p = (TaskAttempt) proto;
        if (p.hasOutput()) {
            output = VariableValueModel.fromProto(p.getOutput());
        }
        if (p.hasScheduleTime()) {
            scheduleTime = LHUtil.fromProtoTs(p.getScheduleTime());
        }
        if (p.hasLogOutput()) {
            logOutput = VariableValueModel.fromProto(p.getLogOutput());
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

    public TaskAttempt.Builder toProto() {
        TaskAttempt.Builder out = TaskAttempt.newBuilder();

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
