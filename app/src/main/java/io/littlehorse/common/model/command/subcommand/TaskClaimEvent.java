package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.TaskClaimReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskClaimEventPb;
import io.littlehorse.common.proto.TaskClaimEventPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class TaskClaimEvent extends SubCommand<TaskClaimEventPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;

    public Class<TaskClaimEventPb> getProtoBaseClass() {
        return TaskClaimEventPb.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public TaskClaimEventPb.Builder toProto() {
        TaskClaimEventPb.Builder b = TaskClaimEventPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setTime(LHUtil.fromDate(time));
        return b;
    }

    public boolean hasResponse() {
        // TODO: It's wasteful to always put a response here, since when the
        // taskdef's queue type is "KAFKA", no one looks at the response.
        return true;
    }

    public TaskClaimReply process(LHDAO dao, LHConfig config) {
        LHUtil.log("Hello from the process call for taskclaimevent");
        TaskClaimReply out = new TaskClaimReply();

        WfRun wfRun = dao.getWfRun(wfRunId);
        if (wfRun == null) {
            LHUtil.log("WARN: Got taskResult for non-existent wfRun", wfRunId);
            return null;
        }

        // Now need to confirm that no one's claimed this task yet.
        NodeRun thingToClaim = dao.getNodeRun(
            wfRunId,
            threadRunNumber,
            taskRunPosition
        );
        if (thingToClaim.status != LHStatusPb.STARTING) {
            // then it's already been claimed.
            out.message = "Unable to claim task, try again.";
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            return out;
        }

        WfSpec wfSpec = dao.getWfSpec(wfRun.wfSpecName, wfRun.wfSpecVersion);
        if (wfSpec == null) {
            LHUtil.log(
                "WARN: Got WfRun with missing WfSpec, should be impossible: ",
                wfRunId
            );
            return null;
        }

        // Needs to be done before we process the event, since processing the event
        // will delete the task schedule request.
        out.result =
            dao.getTaskScheduleRequest(wfRunId, threadRunNumber, taskRunPosition);
        out.code = LHResponseCodePb.OK;

        wfRun.wfSpec = wfSpec;
        wfRun.cmdDao = dao;
        wfRun.processTaskStart(this);
        return out;
    }

    public static TaskClaimEvent fromProto(TaskClaimEventPbOrBuilder proto) {
        TaskClaimEvent out = new TaskClaimEvent();
        out.initFrom(proto);
        return out;
    }

    public void initFrom(MessageOrBuilder p) {
        TaskClaimEventPbOrBuilder proto = (TaskClaimEventPbOrBuilder) p;
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
