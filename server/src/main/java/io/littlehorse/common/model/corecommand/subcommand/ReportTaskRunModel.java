package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.corecommand.failure.LHTaskErrorModel;
import io.littlehorse.common.model.corecommand.failure.LHTaskExceptionModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportTaskRunModel extends SubCommand<ReportTaskRun> {

    private TaskRunIdModel taskRunId;
    private Date time;
    private TaskStatus status;
    private VariableValueModel stdout;
    private VariableValueModel stderr;
    private int attemptNumber; // this is CRUCIAL to set properly.
    private LHTaskErrorModel error;
    private LHTaskExceptionModel exception;

    public String getPartitionKey() {
        return taskRunId.getPartitionKey().get();
    }

    public Class<ReportTaskRun> getProtoBaseClass() {
        return ReportTaskRun.class;
    }

    public boolean hasResponse() {
        return true;
    }

    @Override
    public Empty process(CoreProcessorDAO dao, LHServerConfig config, String tenantId) {
        TaskRunModel task = dao.get(taskRunId);
        if (task == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Provided taskRunId was invalid");
        }

        task.updateTaskResult(this);
        return Empty.getDefaultInstance();
    }

    public ReportTaskRun.Builder toProto() {
        ReportTaskRun.Builder b = ReportTaskRun.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTime(LHUtil.fromDate(time))
                .setStatus(status)
                .setAttemptNumber(attemptNumber);

        if (stdout != null) b.setOutput(stdout.toProto());
        if (stderr != null) b.setLogOutput(stderr.toProto());
        if (error != null) b.setError(error.toProto());
        if (exception != null) b.setException(exception.toProto());

        return b;
    }

    public void initFrom(Message proto) {
        ReportTaskRun p = (ReportTaskRun) proto;
        this.taskRunId = TaskRunIdModel.fromProto(p.getTaskRunId(), TaskRunIdModel.class);
        this.time = LHUtil.fromProtoTs(p.getTime());
        this.status = p.getStatus();
        this.attemptNumber = p.getAttemptNumber();

        if (p.hasOutput()) {
            this.stdout = VariableValueModel.fromProto(p.getOutput());
        }

        if (p.hasLogOutput()) {
            this.stderr = VariableValueModel.fromProto(p.getLogOutput());
        }

        if (p.hasError()) {
            this.error = LHSerializable.fromProto(p.getError(), LHTaskErrorModel.class);
        }

        if (p.hasException()) {
            this.exception = LHSerializable.fromProto(p.getException(), LHTaskExceptionModel.class);
        }
    }

    public static ReportTaskRunModel fromProto(ReportTaskRun proto) {
        ReportTaskRunModel out = new ReportTaskRunModel();
        out.initFrom(proto);
        return out;
    }
}
