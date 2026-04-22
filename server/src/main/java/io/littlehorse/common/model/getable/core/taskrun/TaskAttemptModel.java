package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.corecommand.failure.LHTaskErrorModel;
import io.littlehorse.common.model.corecommand.failure.LHTaskExceptionModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Date;
import java.util.Optional;

public class TaskAttemptModel extends LHSerializable<TaskAttempt> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskAttemptModel.class);
    private VariableValueModel output;
    private VariableValueModel logOutput;

    private Date scheduleTime;
    private Date startTime;
    private Date endTime;
    private String taskWorkerId;
    private String taskWorkerVersion;
    private TaskStatus status;
    private LHTaskExceptionModel exception;
    private LHTaskErrorModel error;
    private boolean maskedValue;

    // Transient: not serialized to proto. Set when the attempt is created (enters PENDING state).
    // Used for pending_to_scheduled latency metrics. May be null if loaded from store.
    private Date pendingTime;

    public TaskAttemptModel() {
        this.status = TaskStatus.TASK_PENDING;
        this.pendingTime = new Date();
    }

    @Override
    public Class<TaskAttempt> getProtoBaseClass() {
        return TaskAttempt.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskAttempt p = (TaskAttempt) proto;
        maskedValue = p.getMaskedValue();
        if (p.hasOutput()) {
            if (maskedValue && context instanceof RequestExecutionContext) {
                output = new VariableValueModel(LHConstants.STRING_MASK);
            } else {
                output = VariableValueModel.fromProto(p.getOutput(), context);
            }
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
        taskWorkerId = p.getTaskWorkerId();
        status = p.getStatus();
        if (p.hasError()) error = LHSerializable.fromProto(p.getError(), LHTaskErrorModel.class, context);
        if (p.hasException())
            exception = LHSerializable.fromProto(p.getException(), LHTaskExceptionModel.class, context);
    }

    @Override
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
        if (error != null) {
            out.setError(error.toProto());
        }
        if (exception != null) {
            out.setException(exception.toProto());
        }

        out.setStatus(status);
        out.setMaskedValue(maskedValue);

        return out;
    }

    public boolean containsException() {
        return exception != null;
    }

    /**
     * If the TaskAttempt failed, returns the failureCode with which it failed. Note that this is
     * NOT the `LHErrorType` enum, because it can include user-defined business EXCEPTIONs.
     * @return the failure code with which the taskAttempt failed.
     */
    public String getFailureCode() {
        switch (this.getStatus()) {
            case TASK_EXCEPTION:
                return this.getException().getName();
            case TASK_FAILED:
                return LHConstants.TASK_FAILURE;
            case TASK_TIMEOUT:
                return LHConstants.TIMEOUT;
            case TASK_OUTPUT_SERDE_ERROR:
                return LHConstants.VAR_MUTATION_ERROR;
            case TASK_INPUT_VAR_SUB_ERROR:
                return LHConstants.VAR_SUB_ERROR;
            case TASK_RUNNING:
            case TASK_SCHEDULED:
            case TASK_SUCCESS:
            case TASK_PENDING:
            case UNRECOGNIZED:
        }
        log.trace("Called getFailureCodeFor() on non-failed TaskAttempt. Probably you need more coffee!");
        return null;
    }

    /**
     * If this TaskAttempt failed, returns the human-readable message with which it failed.
     * @return the human-readable message with which this TaskAttempt failed.
     */
    public String getFailureMessage() {
        switch (this.getStatus()) {
            case TASK_EXCEPTION:
                return this.getException().getMessage();
            case TASK_FAILED:
                return "Task execution failed";
            case TASK_TIMEOUT:
                return "Task timed out";
            case TASK_OUTPUT_SERDE_ERROR:
                return "Failed serializing or deserializing Task Output";
            case TASK_INPUT_VAR_SUB_ERROR:
                return "Failed calculating Task Input Variables";
            case TASK_RUNNING:
            case TASK_SCHEDULED:
            case TASK_SUCCESS:
            case UNRECOGNIZED:
            case TASK_PENDING:
        }
        log.trace("Called getFailureMessage() on non-failed TaskAttempt. Probably you need more coffee!");
        return null;
    }

    public Optional<VariableValueModel> getFailureContent() {
        if (this.getStatus().equals(TaskStatus.TASK_EXCEPTION)) {
            return Optional.ofNullable(this.getException().getContent());
        }
        return Optional.empty();
    }

    public VariableValueModel getOutput() {
        return this.output;
    }

    public VariableValueModel getLogOutput() {
        return this.logOutput;
    }

    public Date getScheduleTime() {
        return this.scheduleTime;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public String getTaskWorkerId() {
        return this.taskWorkerId;
    }

    public String getTaskWorkerVersion() {
        return this.taskWorkerVersion;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public LHTaskExceptionModel getException() {
        return this.exception;
    }

    public LHTaskErrorModel getError() {
        return this.error;
    }

    public boolean isMaskedValue() {
        return this.maskedValue;
    }

    public Date getPendingTime() {
        return this.pendingTime;
    }

    public void setOutput(final VariableValueModel output) {
        this.output = output;
    }

    public void setLogOutput(final VariableValueModel logOutput) {
        this.logOutput = logOutput;
    }

    public void setScheduleTime(final Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    public void setTaskWorkerId(final String taskWorkerId) {
        this.taskWorkerId = taskWorkerId;
    }

    public void setTaskWorkerVersion(final String taskWorkerVersion) {
        this.taskWorkerVersion = taskWorkerVersion;
    }

    public void setStatus(final TaskStatus status) {
        this.status = status;
    }

    public void setException(final LHTaskExceptionModel exception) {
        this.exception = exception;
    }

    public void setError(final LHTaskErrorModel error) {
        this.error = error;
    }

    public void setMaskedValue(final boolean maskedValue) {
        this.maskedValue = maskedValue;
    }

    public void setPendingTime(final Date pendingTime) {
        this.pendingTime = pendingTime;
    }
}
