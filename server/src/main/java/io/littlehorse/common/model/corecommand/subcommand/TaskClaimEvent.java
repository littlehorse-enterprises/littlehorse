package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.proto.TaskClaimEventPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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
public class TaskClaimEvent extends CoreSubCommand<TaskClaimEventPb> {

    private TaskRunIdModel taskRunId;
    private Date time;
    private String taskWorkerVersion;
    private String taskWorkerId;

    public TaskClaimEvent() {}

    public TaskClaimEvent(ScheduledTaskModel task, PollTaskRequestObserver taskClaimer) {
        this.taskRunId = task.getTaskRunId();
        this.time = new Date();
        this.taskWorkerId = taskClaimer.getClientId();
        this.taskWorkerVersion = taskClaimer.getTaskWorkerVersion();
    }

    public Class<TaskClaimEventPb> getProtoBaseClass() {
        return TaskClaimEventPb.class;
    }

    @Override
    public String getPartitionKey() {
        return taskRunId.getPartitionKey().get();
    }

    public TaskClaimEventPb.Builder toProto() {
        TaskClaimEventPb.Builder b = TaskClaimEventPb.newBuilder()
                .setTaskRunId(taskRunId.toProto())
                .setTaskWorkerVersion(taskWorkerVersion)
                .setTaskWorkerId(taskWorkerId)
                .setTime(LHUtil.fromDate(time));
        return b;
    }

    @Override
    public PollTaskResponse process(CoreProcessorContext executionContext, LHServerConfig config) {
        TaskRunModel taskRun = executionContext.getableManager().get(taskRunId);
        if (taskRun == null) {
            log.debug("Got claimTask for non-existent taskRun {}", taskRunId);
            throw new LHApiException(Status.INVALID_ARGUMENT, "Got claimTask for nonexistent taskRun {}" + taskRunId);
        }

        // Needs to be done before we process the event, since processing the event
        // will delete the task schedule request.
        ScheduledTaskModel scheduledTask = executionContext.getTaskManager().markTaskAsScheduled(taskRun);

        // It's totally fine for the scheduledTask to be null. That happens when someone already
        // claimed that task. This happens when a server is recovering from a crash. The fact that it
        // is null prevents it from being scheduled twice.
        //
        // We shouldn't throw an error on this, we just return an empty optional.
        if (scheduledTask == null) {
            log.debug("Processing pollTaskRequest for task {} that was already claimed", taskRunId);
            return PollTaskResponse.newBuilder().build();
        } else {
            taskRun.onTaskAttemptStarted(this);
            executionContext.getableManager().get(taskRunId.wfRunId).advance(time);
            return PollTaskResponse.newBuilder()
                    .setResult(scheduledTask.toProto())
                    .build();
        }
    }

    public static TaskClaimEvent fromProto(TaskClaimEventPb proto, ExecutionContext context) {
        TaskClaimEvent out = new TaskClaimEvent();
        out.initFrom(proto, context);
        return out;
    }

    public void initFrom(Message p, ExecutionContext context) {
        TaskClaimEventPb proto = (TaskClaimEventPb) p;
        taskRunId = LHSerializable.fromProto(proto.getTaskRunId(), TaskRunIdModel.class, context);
        this.taskWorkerVersion = proto.getTaskWorkerVersion();
        this.taskWorkerId = proto.getTaskWorkerId();
        this.time = LHUtil.fromProtoTs(proto.getTime());
    }
}
