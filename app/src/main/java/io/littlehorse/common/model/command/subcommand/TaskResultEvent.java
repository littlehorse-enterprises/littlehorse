package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.ReportTaskReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.TaskResultEventPb;
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

    public ReportTaskReply process(LHDAO dao, LHConfig config) {
        // First, get the WfRun
        WfRun wfRun = dao.getWfRun(wfRunId);
        ReportTaskReply out = new ReportTaskReply();

        if (wfRun == null) {
            out.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            out.message = "Provided invalid wfRunId";
            return out;
        }
        wfRun.cmdDao = dao;

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            wfRun.threadRuns
                .get(0)
                .fail(
                    new Failure(
                        TaskResultCodePb.INTERNAL_ERROR,
                        "Appears wfSpec was deleted",
                        LHConstants.INTERNAL_ERROR
                    ),
                    new Date()
                );
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            out.message = "Apparently WfSpec was deleted!";
            return out;
        }

        wfRun.wfSpec = wfSpec;
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

    public void initFrom(Message p) {
        TaskResultEventPb proto = (TaskResultEventPb) p;
        this.wfRunId = proto.getWfRunId();
        this.threadRunNumber = proto.getThreadRunNumber();
        this.taskRunPosition = proto.getTaskRunPosition();
        this.time = LHUtil.fromProtoTs(proto.getTime());
        this.resultCode = proto.getResultCode();
        this.fromRpc = proto.getFromRpc();

        if (proto.hasOutput()) {
            this.stdout = VariableValue.fromProto(proto.getOutput());
        }
        if (proto.hasLogOutput()) {
            this.stderr = VariableValue.fromProto(proto.getLogOutput());
        }
    }

    public static TaskResultEvent fromProto(TaskResultEventPb proto) {
        TaskResultEvent out = new TaskResultEvent();
        out.initFrom(proto);
        return out;
    }
}
