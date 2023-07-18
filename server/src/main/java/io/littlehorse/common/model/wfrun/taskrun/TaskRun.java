package io.littlehorse.common.model.wfrun.taskrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.ReportTaskRun;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommandresponse.ReportTaskReply;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.objectId.TaskDefId;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.TaskAttempt;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.TaskAttemptPb;
import io.littlehorse.sdk.common.proto.TaskRunPb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import io.littlehorse.sdk.common.proto.VarNameAndValPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class TaskRun extends Getable<TaskRunPb> {

    private TaskRunId id;
    private List<TaskAttempt> attempts;
    private int maxAttempts;
    private String taskDefName;
    private List<VarNameAndVal> inputVariables;
    private TaskRunSource taskRunSource;
    private Date scheduledAt;
    private int timeoutSeconds;
    private TaskStatusPb status;

    public Class<TaskRunPb> getProtoBaseClass() {
        return TaskRunPb.class;
    }

    public void initFrom(Message proto) {
        TaskRunPb p = (TaskRunPb) proto;
        taskDefName = p.getTaskDefName();
        maxAttempts = p.getMaxAttempts();
        scheduledAt = LHUtil.fromProtoTs(p.getScheduledAt());
        id = LHSerializable.fromProto(p.getId(), TaskRunId.class);
        status = p.getStatus();
        timeoutSeconds = p.getTimeoutSeconds();

        for (TaskAttemptPb attempt : p.getAttemptsList()) {
            attempts.add(LHSerializable.fromProto(attempt, TaskAttempt.class));
        }
        for (VarNameAndValPb v : p.getInputVariablesList()) {
            inputVariables.add(LHSerializable.fromProto(v, VarNameAndVal.class));
        }
    }

    public TaskRunPb.Builder toProto() {
        TaskRunPb.Builder out = TaskRunPb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setMaxAttempts(maxAttempts)
            .setScheduledAt(LHUtil.fromDate(scheduledAt))
            .setStatus(status)
            .setTimeoutSeconds(timeoutSeconds)
            .setId(id.toProto());

        for (VarNameAndVal v : inputVariables) {
            out.addInputVariables(v.toProto());
        }
        for (TaskAttempt attempt : attempts) {
            out.addAttempts(attempt.toProto());
        }

        return out;
    }

    public Date getCreatedAt() {
        return scheduledAt;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of(
            new GetableIndex<>(
                List.of(Pair.of("taskDefName", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.LOCAL)
            ),
            new GetableIndex<>(
                List.of(
                    Pair.of("taskDefName", GetableIndex.ValueType.SINGLE),
                    Pair.of("status", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL)
            )
            // NOTE: we're not indexing just based on status because we don't want
            // to have too many reads/writes in RocksDB as those are expensive.
            //
            // Additionally, we could index based on the number of retries, so that
            // we can find all TaskRun's that have been retried. But that maybe can
            // be in the 0.1.1 release, not 0.1.0
        );
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        switch (key) {
            case "taskDefName" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getTaskDefName(),
                        TagStorageTypePb.LOCAL
                    )
                );
            }
            case "status" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.status.toString(),
                        TagStorageTypePb.LOCAL
                    )
                );
            }
        }
        log.warn("Received unknown key for TaskRun Index: {}", key);
        return null;
    }

    // Not in the proto
    private LHDAO dao;

    public TaskRun() {
        scheduledAt = new Date();
        inputVariables = new ArrayList<>();
        attempts = new ArrayList<>();
    }

    public TaskRun(
        LHDAO dao,
        List<VarNameAndVal> inputVars,
        TaskRunSource source,
        TaskNode node
    ) {
        this();
        this.inputVariables = inputVars;
        this.taskRunSource = source;
        this.setDao(dao);
        this.taskDefName = node.getTaskDefName();
        this.maxAttempts = node.getRetries() + 1;
        this.status = TaskStatusPb.TASK_SCHEDULED;
        this.timeoutSeconds = node.getTimeoutSeconds();
    }

    @Override
    public TaskRunId getObjectId() {
        return id;
    }

    public static TaskRun fromProto(TaskRunPb proto) {
        TaskRun out = new TaskRun();
        out.initFrom(proto);
        return out;
    }

    public TaskAttempt getLatestAttempt() {
        if (attempts.isEmpty()) return null;
        return attempts.get(attempts.size() - 1);
    }

    public boolean shouldRetry() {
        // Shouldn't look at nodeRun to decide whether to retry task or not.
        TaskAttempt latest = getLatestAttempt();
        if (latest == null) {
            // This really shouldn't happen I think
            return false;
        }

        if (
            latest.getStatus() != TaskStatusPb.TASK_FAILED &&
            latest.getStatus() != TaskStatusPb.TASK_TIMEOUT
        ) {
            // Can only retry timeout or task failure.
            return false;
        }

        return attempts.size() < maxAttempts;
    }

    public void scheduleAttempt() {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setVariables(inputVariables);
        scheduledTask.setAttemptNumber(attempts.size());
        scheduledTask.setCreatedAt(new Date());
        scheduledTask.setSource(taskRunSource);
        scheduledTask.setTaskDefId(new TaskDefId(taskDefName));
        scheduledTask.setTaskRunId(id);

        // initialization happens here
        attempts.add(new TaskAttempt());

        getDao().scheduleTask(scheduledTask);
        // TODO: Update Metrics
    }

    public void processStart(TaskClaimEvent se) {
        this.status = TaskStatusPb.TASK_RUNNING;

        // create a timer to mark the task is timeout if it does not finish
        ReportTaskRun taskResult = new ReportTaskRun();
        taskResult.setTaskRunId(id);
        taskResult.setTime(
            new Date(System.currentTimeMillis() + (1000 * timeoutSeconds))
        );
        taskResult.setStatus(TaskStatusPb.TASK_TIMEOUT);

        LHTimer timer = new LHTimer(
            new Command(taskResult, taskResult.getTime()),
            getDao()
        );
        getDao().scheduleTimer(timer);

        // Now that that's out of the way, we can mark the TaskRun as running.
        // Also we need to save the task worker version and client id.
        TaskAttempt attempt = getLatestAttempt();
        attempt.setTaskWorkerId(se.getTaskWorkerId());
        attempt.setTaskWorkerVersion(se.getTaskWorkerVersion());
        attempt.setStartTime(se.getTime());
        attempt.setStatus(TaskStatusPb.TASK_RUNNING);
    }

    public ReportTaskReply updateTaskResult(ReportTaskRun ce) {
        if (ce.getAttemptNumber() >= attempts.size()) {
            return new ReportTaskReply(
                LHResponseCodePb.BAD_REQUEST_ERROR,
                "Couldn't find specified Task Attempt. Bad client!"
            );
        }

        TaskAttempt attempt = attempts.get(ce.getAttemptNumber());

        if (attempt.getStatus() != TaskStatusPb.TASK_RUNNING) {
            // The task result has already been processed, so ignore this event.
            // Example:
            // 1. Timer event coming to signify timeout on a task that successfully
            //    completed already.
            // 2. For TIMEOUT tasks, it's possible the TASK_COMPLETED event was late.
            //    Currently, we don't resurrect the workflow. However, in the future,
            //    we may want to do something more with it, but it gets really
            //    complicated very quickly from a user semantics perspective.
            log.debug(
                "Ignored {} TaskRunResult on TaskRun {} attempt no. {} w/status {}",
                ce.getStatus(),
                id.getStoreKey(),
                ce.getAttemptNumber(),
                attempt.getStatus()
            );
            return new ReportTaskReply(LHResponseCodePb.OK, null);
        }

        attempt.setOutput(ce.getStdout());
        attempt.setLogOutput(ce.getStderr());
        attempt.setStatus(ce.getStatus());
        attempt.setEndTime(ce.getTime());

        if (ce.getStatus() == TaskStatusPb.TASK_SUCCESS) {
            // Tell the WfRun that the TaskRun is done.
            taskRunSource.getSubSource().onCompleted(attempt, getDao());
            status = TaskStatusPb.TASK_SUCCESS;
            return new ReportTaskReply(LHResponseCodePb.OK, null);
        }

        if (shouldRetry()) {
            status = TaskStatusPb.TASK_SCHEDULED;
            scheduleAttempt();
        } else {
            status = ce.getStatus();
            taskRunSource.getSubSource().onFailed(attempt, getDao());
        }
        return new ReportTaskReply(LHResponseCodePb.OK, null);
    }
}
