package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.ReportTaskReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.TaskResultEventPb;
import io.littlehorse.common.proto.TaskResultEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.CommandProcessorDao;
import java.util.Date;

public class TaskResultEvent extends SubCommand<TaskResultEventPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;
    public VariableValue stdout;
    public VariableValue stderr;
    public TaskResultCodePb resultCode;
    public boolean fromRpc;

    public String getPartitionKey() {
        return wfRunId;
    }

    public Class<TaskResultEventPb> getProtoBaseClass() {
        return TaskResultEventPb.class;
    }

    public Integer getThreadRunNumber() {
        return threadRunNumber;
    }

    public Integer getNodeRunPosition() {
        return taskRunPosition;
    }

    public boolean hasResponse() {
        return fromRpc;
    }

    public ReportTaskReply process(CommandProcessorDao dao, LHConfig config) {
        // First, get the WfRun
        WfRun wfRun = dao.getWfRun(wfRunId);
        ReportTaskReply out = new ReportTaskReply();

        if (wfRun == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Somehow missing wfSpec for wfRun";
            return out;
        }

        wfRun.wfSpec = wfSpec;
        wfRun.cmdDao = dao;
        wfRun.processTaskResult(this);

        out.code = LHResponseCodePb.OK;
        return out;
    }

    public TaskResultEventPb.Builder toProto() {
        TaskResultEventPb.Builder b = TaskResultEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time))
            .setResultCode(resultCode)
            .setFromRpc(fromRpc);

        if (stdout != null) b.setOutput(stdout.toProto());
        if (stderr != null) b.setLogOutput(stderr.toProto());

        return b;
    }

    public void initFrom(MessageOrBuilder p) {
        TaskResultEventPbOrBuilder proto = (TaskResultEventPbOrBuilder) p;
        this.wfRunId = proto.getWfRunId();
        this.threadRunNumber = proto.getThreadRunNumber();
        this.taskRunPosition = proto.getTaskRunPosition();
        this.time = LHUtil.fromProtoTs(proto.getTime());
        this.resultCode = proto.getResultCode();
        this.fromRpc = proto.getFromRpc();

        if (proto.hasOutput()) {
            this.stdout = VariableValue.fromProto(proto.getOutputOrBuilder());
        }
        if (proto.hasLogOutput()) {
            this.stderr = VariableValue.fromProto(proto.getLogOutput());
        }
    }

    public static TaskResultEvent fromProto(TaskResultEventPbOrBuilder proto) {
        TaskResultEvent out = new TaskResultEvent();
        out.initFrom(proto);
        return out;
    }
}
