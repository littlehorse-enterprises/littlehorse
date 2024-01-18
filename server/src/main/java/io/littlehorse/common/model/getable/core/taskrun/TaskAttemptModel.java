package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.corecommand.failure.LHTaskErrorModel;
import io.littlehorse.common.model.corecommand.failure.LHTaskExceptionModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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
    private String taskWorkerVersion;
    private TaskStatus status;
    private LHTaskExceptionModel exception;
    private LHTaskErrorModel error;

    public TaskAttemptModel() {
        scheduleTime = new Date();
        status = TaskStatus.TASK_SCHEDULED;
    }

    public Class<TaskAttempt> getProtoBaseClass() {
        return TaskAttempt.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskAttempt p = (TaskAttempt) proto;
        if (p.hasOutput()) {
            output = VariableValueModel.fromProto(p.getOutput(), context);
        }
        if (p.hasScheduleTime()) {
            scheduleTime = LHUtil.fromProtoTs(p.getScheduleTime());
        }
        if (p.hasLogOutput()) {
            logOutput = VariableValueModel.fromProto(p.getLogOutput(), context);
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
        status = p.getStatus();
        if (p.hasError()) error = LHSerializable.fromProto(p.getError(), LHTaskErrorModel.class, context);
        if (p.hasException())
            exception = LHSerializable.fromProto(p.getException(), LHTaskExceptionModel.class, context);
    }

    public TaskAttempt.Builder toProto() {
        TaskAttempt.Builder out = TaskAttempt.newBuilder();

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
        if (error != null) {
            out.setError(error.toProto());
        }
        if (exception != null) {
            out.setException(exception.toProto());
        }

        out.setStatus(status);

        return out;
    }

    public boolean containsException() {
        return exception != null;
    }
}
