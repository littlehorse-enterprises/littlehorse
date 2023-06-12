package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.TaskClaimReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.observabilityevent.ObservabilityEvent;
import io.littlehorse.common.model.observabilityevent.events.TaskStartOe;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.TaskClaimEventPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import java.util.Date;

public class TaskClaimEvent extends SubCommand<TaskClaimEventPb> {

    public String wfRunId;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public Date time;
    public String taskWorkerVersion;
    public String taskWorkerId;

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
            .setTaskWorkerVersion(taskWorkerVersion)
            .setTaskWorkerId(taskWorkerId)
            .setTime(LHUtil.fromDate(time));
        return b;
    }

    public boolean hasResponse() {
        // TODO: It's wasteful to always put a response here, since when the
        // taskdef's queue type is "KAFKA", no one looks at the response.
        return true;
    }

    public TaskClaimReply process(LHDAO dao, LHConfig config) {
        TaskClaimReply out = new TaskClaimReply();

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

        // Needs to be done before we process the event, since processing the event
        // will delete the task schedule request.
        ScheduledTask scheduledTask = dao.markTaskAsScheduled(
            wfRunId,
            threadRunNumber,
            taskRunPosition
        );

        if (scheduledTask == null) {
            // That means the task has been taken already.
            out.message = "Unable to claim this task, someone beat you to it";
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
            return out;
        }

        out.result = scheduledTask;
        out.code = LHResponseCodePb.OK;

        TaskStartOe oe = new TaskStartOe();
        oe.taskRunPosition = taskRunPosition;
        oe.threadRunNumber = threadRunNumber;
        oe.workerId = "TODO: pass this to taskClaimEvent";
        // TODO LH-338: re-support observability events
        // dao.addObservabilityEvent(new ObservabilityEvent(wfRunId, oe));

        wfRun.wfSpec = wfSpec;
        wfRun.cmdDao = dao;
        wfRun.processTaskStart(this);
        return out;
    }

    public static TaskClaimEvent fromProto(TaskClaimEventPb proto) {
        TaskClaimEvent out = new TaskClaimEvent();
        out.initFrom(proto);
        return out;
    }

    public void initFrom(Message p) {
        TaskClaimEventPb proto = (TaskClaimEventPb) p;
        this.wfRunId = proto.getWfRunId();
        this.threadRunNumber = proto.getThreadRunNumber();
        this.taskRunNumber = proto.getTaskRunNumber();
        this.taskRunPosition = proto.getTaskRunPosition();
        this.taskWorkerVersion = proto.getTaskWorkerVersion();
        this.taskWorkerId = proto.getTaskWorkerId();
        this.time = LHUtil.fromProtoTs(proto.getTime());
    }

    public Integer getThreadRunNumber() {
        return threadRunNumber;
    }

    public Integer getNodeRunPosition() {
        return taskRunPosition;
    }
}
