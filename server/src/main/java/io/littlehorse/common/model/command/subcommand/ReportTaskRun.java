package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.ReportTaskReply;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.ReportTaskRunPb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportTaskRun extends SubCommand<ReportTaskRunPb> {

    private TaskRunId taskRunId;
    private Date time;
    private TaskStatusPb status;
    private VariableValueModel stdout;
    private VariableValueModel stderr;
    private int attemptNumber; // this is CRUCIAL to set properly.

    public String getPartitionKey() {
        return taskRunId.getPartitionKey();
    }

    public Class<ReportTaskRunPb> getProtoBaseClass() {
        return ReportTaskRunPb.class;
    }

    public boolean hasResponse() {
        return true;
    }

    public ReportTaskReply process(LHDAO dao, LHConfig config) {
        ReportTaskReply out = new ReportTaskReply();

        TaskRun task = dao.getTaskRun(taskRunId);
        if (task == null) {
            out.setCode(LHResponseCodePb.BAD_REQUEST_ERROR);
            out.setMessage("Could not find specified taskrun. Bad client!");
            return out;
        }

        return task.updateTaskResult(this);
    }

    public ReportTaskRunPb.Builder toProto() {
        ReportTaskRunPb.Builder b = ReportTaskRunPb
            .newBuilder()
            .setTaskRunId(taskRunId.toProto())
            .setTime(LHUtil.fromDate(time))
            .setStatus(status)
            .setAttemptNumber(attemptNumber);

        if (stdout != null) b.setOutput(stdout.toProto());
        if (stderr != null) b.setLogOutput(stderr.toProto());

        return b;
    }

    public void initFrom(Message proto) {
        ReportTaskRunPb p = (ReportTaskRunPb) proto;
        this.taskRunId = TaskRunId.fromProto(p.getTaskRunId(), TaskRunId.class);
        this.time = LHUtil.fromProtoTs(p.getTime());
        this.status = p.getStatus();
        this.attemptNumber = p.getAttemptNumber();

        if (p.hasOutput()) {
            this.stdout = VariableValueModel.fromProto(p.getOutput());
        }

        if (p.hasLogOutput()) {
            this.stderr = VariableValueModel.fromProto(p.getLogOutput());
        }
    }

    public static ReportTaskRun fromProto(ReportTaskRunPb proto) {
        ReportTaskRun out = new ReportTaskRun();
        out.initFrom(proto);
        return out;
    }
}
