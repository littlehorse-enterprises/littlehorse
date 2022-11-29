package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.TaskStartedEventPb;
import io.littlehorse.common.proto.TaskStartedEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.CommandProcessorDao;
import java.util.Date;

public class TaskStartedEvent extends SubCommand<TaskStartedEventPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;

    public Class<TaskStartedEventPb> getProtoBaseClass() {
        return TaskStartedEventPb.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public TaskStartedEventPb.Builder toProto() {
        TaskStartedEventPb.Builder b = TaskStartedEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time));
        return b;
    }

    public boolean hasResponse() {
        return false;
    }

    public LHSerializable<?> process(CommandProcessorDao dao, LHConfig config) {
        WfRun wfRun = dao.getWfRun(wfRunId);
        if (wfRun == null) {
            LHUtil.log("WARN: Got taskResult for non-existent wfRun", wfRunId);
            return null;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            LHUtil.log(
                "WARN: Got WfRun with missing WfSpec, should be impossible: ",
                wfRunId
            );
            return null;
        }

        wfRun.wfSpec = wfSpec;
        wfRun.cmdDao = dao;
        wfRun.processTaskStart(this);

        return null;
    }

    public static TaskStartedEvent fromProto(TaskStartedEventPbOrBuilder proto) {
        TaskStartedEvent out = new TaskStartedEvent();
        out.initFrom(proto);
        return out;
    }

    public void initFrom(MessageOrBuilder p) {
        TaskStartedEventPbOrBuilder proto = (TaskStartedEventPbOrBuilder) p;
        this.wfRunId = proto.getWfRunId();
        this.threadRunNumber = proto.getThreadRunNumber();
        this.taskRunNumber = proto.getTaskRunNumber();
        this.taskRunPosition = proto.getTaskRunPosition();
        this.time = LHUtil.fromProtoTs(proto.getTime());
    }

    public Integer getThreadRunNumber() {
        return threadRunNumber;
    }

    public Integer getNodeRunPosition() {
        return taskRunPosition;
    }
}
