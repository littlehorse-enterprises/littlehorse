package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.TaskClaimReply;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.proto.TaskClaimEventPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.server.streamsimpl.taskqueue.PollTaskRequestObserver;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
/*
 * In certain crash-failure scenarios, it is possible for there to be two
 * in-flight
 * TaskClaimEvent's. However, that's not a problem, because the processing of
 * a TaskClaimEvent is strictly ordered, so if there are multiple in flight only
 * one will receive the `ScheduledTask`, and therefore the second will be
 * ignored.
 */
public class TaskClaimEvent extends SubCommand<TaskClaimEventPb> {

    private TaskRunIdModel taskRunId;
    private Date time;
    private String taskWorkerVersion;
    private String taskWorkerId;

    public TaskClaimEvent() {
    }

    public TaskClaimEvent(ScheduledTaskModel task, PollTaskRequestObserver taskClaimer) {
        this.taskRunId = task.getTaskRunId();
        this.time = new Date();
        this.taskWorkerId = taskClaimer.getClientId();
        this.taskWorkerVersion = taskClaimer.getTaskWorkerVersion();
    }

    public Class<TaskClaimEventPb> getProtoBaseClass() {
        return TaskClaimEventPb.class;
    }

    public String getPartitionKey() {
        return taskRunId.getPartitionKey();
    }

    public TaskClaimEventPb.Builder toProto() {
        TaskClaimEventPb.Builder b = TaskClaimEventPb.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTaskWorkerVersion(taskWorkerVersion)
                .setTaskWorkerId(taskWorkerId)
                .setTime(LHUtil.fromDate(time));
        return b;
    }

    public boolean hasResponse() {
        // TaskClaimEvents are always due to a Task Worker's poll request.
        return true;
    }

    public TaskClaimReply process(CoreProcessorDAO dao, LHConfig config) {
        TaskClaimReply out = new TaskClaimReply();

        TaskRunModel taskRun = dao.getTaskRun(taskRunId);
        if (taskRun == null) {
            log.warn("Got claimTask for non-existent taskRun {}", taskRunId);
            out.setCode(LHResponseCode.BAD_REQUEST_ERROR);
            out.setMessage("Couldn't find specified TaskRun");
            return out;
        }

        // Needs to be done before we process the event, since processing the event
        // will delete the task schedule request.
        ScheduledTaskModel scheduledTask = dao.markTaskAsScheduled(taskRunId);

        if (scheduledTask == null) {
            // That means the task has been taken already.
            out.setMessage("Unable to claim this task, someone beat you to it");
            out.setCode(LHResponseCode.NOT_FOUND_ERROR);
            return out;
        }

        taskRun.processStart(this);

        out.result = scheduledTask;
        out.code = LHResponseCode.OK;

        // TODO: Task Started Metrics
        return out;
    }

    public static TaskClaimEvent fromProto(TaskClaimEventPb proto) {
        TaskClaimEvent out = new TaskClaimEvent();
        out.initFrom(proto);
        return out;
    }

    public void initFrom(Message p) {
        TaskClaimEventPb proto = (TaskClaimEventPb) p;
        taskRunId = LHSerializable.fromProto(proto.getTaskRunId(), TaskRunIdModel.class);
        this.taskWorkerVersion = proto.getTaskWorkerVersion();
        this.taskWorkerId = proto.getTaskWorkerId();
        this.time = LHUtil.fromProtoTs(proto.getTime());
    }
}
