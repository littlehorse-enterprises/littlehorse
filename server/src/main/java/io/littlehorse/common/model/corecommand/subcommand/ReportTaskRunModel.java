package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.failure.LHTaskErrorModel;
import io.littlehorse.common.model.corecommand.failure.LHTaskExceptionModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportTaskRunModel extends CoreSubCommand<ReportTaskRun> {

    private TaskRunIdModel taskRunId;
    private Date time;
    private TaskStatus status;
    private VariableValueModel output;
    private VariableValueModel logOutput;
    private int attemptNumber; // this is CRUCIAL to set properly.
    private LHTaskErrorModel error;
    private LHTaskExceptionModel exception;

    @Override
    public String getPartitionKey() {
        return taskRunId.getPartitionKey().get();
    }

    @Override
    public Class<ReportTaskRun> getProtoBaseClass() {
        return ReportTaskRun.class;
    }

    @Override
    public Empty process(CoreProcessorContext executionContext, LHServerConfig config) {
        TaskRunModel task = executionContext.getableManager().get(taskRunId);
        if (task == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Provided taskRunId was invalid");
        }

        task.onTaskAttemptResultReported(this);
        return Empty.getDefaultInstance();
    }

    @Override
    public ReportTaskRun.Builder toProto() {
        ReportTaskRun.Builder b = ReportTaskRun.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTime(LHUtil.fromDate(time))
                .setStatus(status)
                .setAttemptNumber(attemptNumber);

        if (output != null) b.setOutput(output.toProto());
        if (logOutput != null) b.setLogOutput(logOutput.toProto());
        if (error != null) b.setError(error.toProto());
        if (exception != null) b.setException(exception.toProto());

        return b;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ReportTaskRun p = (ReportTaskRun) proto;
        this.taskRunId = TaskRunIdModel.fromProto(p.getTaskRunId(), TaskRunIdModel.class, context);
        this.time = LHUtil.fromProtoTs(p.getTime());
        this.status = p.getStatus();
        this.attemptNumber = p.getAttemptNumber();

        if (p.hasOutput()) {
            this.output = VariableValueModel.fromProto(p.getOutput(), context);
        }

        if (p.hasLogOutput()) {
            this.logOutput = VariableValueModel.fromProto(p.getLogOutput(), context);
        }

        if (p.hasError()) {
            this.error = LHSerializable.fromProto(p.getError(), LHTaskErrorModel.class, context);
        }

        if (p.hasException()) {
            this.exception = LHSerializable.fromProto(p.getException(), LHTaskExceptionModel.class, context);
        }
    }

    public static ReportTaskRunModel fromProto(ReportTaskRun proto, ExecutionContext context) {
        ReportTaskRunModel out = new ReportTaskRunModel();
        out.initFrom(proto, context);
        return out;
    }
}
