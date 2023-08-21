package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.corecommand.SubCommand;
import io.littlehorse.common.model.corecommand.subcommandresponse.ReportTaskReply;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
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

    public String getPartitionKey() {
        return taskRunId.getPartitionKey().get();
    }

    public Class<ReportTaskRun> getProtoBaseClass() {
        return ReportTaskRun.class;
    }

    public boolean hasResponse() {
        return true;
    }

    public ReportTaskReply process(CoreProcessorDAO dao, LHConfig config) {
        ReportTaskReply out = new ReportTaskReply();

        TaskRunModel task = dao.get(taskRunId);
        if (task == null) {
            out.setCode(LHResponseCode.BAD_REQUEST_ERROR);
            out.setMessage("Could not find specified taskrun. Bad client!");
            return out;
        }

        return task.updateTaskResult(this);
    }

    public ReportTaskRun.Builder toProto() {
        ReportTaskRun.Builder b = ReportTaskRun.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTime(LHUtil.fromDate(time))
                .setStatus(status)
                .setAttemptNumber(attemptNumber);

        if (stdout != null) b.setOutput(stdout.toProto());
        if (stderr != null) b.setLogOutput(stderr.toProto());

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
    }

    public static ReportTaskRunModel fromProto(ReportTaskRun proto) {
        ReportTaskRunModel out = new ReportTaskRunModel();
        out.initFrom(proto);
        return out;
    }
}
