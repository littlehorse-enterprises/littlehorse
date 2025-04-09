package io.littlehorse.common.model.getable.core.taskrun;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.failure.LHTaskErrorModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskAttemptRetryReadyModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.ExponentialBackoffRetryPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.GetableUpdates;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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
public class TaskRunModel extends CoreGetable<TaskRun> {

    private TaskRunIdModel id;
    private List<TaskAttemptModel> attempts;
    private TaskDefIdModel taskDefId;
    private List<VarNameAndValModel> inputVariables;
    private TaskRunSourceModel taskRunSource;
    private Date scheduledAt;
    private int timeoutSeconds;
    private TaskStatus status;

    private int simpleTotalAttempts;
    private ExponentialBackoffRetryPolicyModel exponentialBackoffRetryPolicy;

    private ExecutionContext executionContext;
    // Only contains value in Processor execution context.
    private ProcessorExecutionContext processorContext;

    public TaskRunModel() {
        scheduledAt = new Date();
        inputVariables = new ArrayList<>();
        attempts = new ArrayList<>();
    }

    public TaskRunModel(
            List<VarNameAndValModel> inputVars,
            TaskRunSourceModel source,
            TaskNodeModel node,
            ProcessorExecutionContext processorContext,
            TaskRunIdModel id,
            TaskDefIdModel taskDefId) {
        this();
        this.inputVariables = inputVars;
        this.taskRunSource = source;
        this.taskDefId = taskDefId;
        this.timeoutSeconds = node.getTimeoutSeconds();
        this.executionContext = processorContext;
        this.processorContext = processorContext;
        this.id = id;

        this.simpleTotalAttempts = 1 + node.getSimpleRetries();
        this.exponentialBackoffRetryPolicy = node.getExponentialBackoffRetryPolicy();

        this.attempts.add(new TaskAttemptModel());

        transitionTo(TaskStatus.TASK_SCHEDULED);
    }

    @Override
    public Class<TaskRun> getProtoBaseClass() {
        return TaskRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskRun p = (TaskRun) proto;
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class, context);
        scheduledAt = LHUtil.fromProtoTs(p.getScheduledAt());
        id = LHSerializable.fromProto(p.getId(), TaskRunIdModel.class, context);
        status = p.getStatus();
        timeoutSeconds = p.getTimeoutSeconds();
        taskRunSource = LHSerializable.fromProto(p.getSource(), TaskRunSourceModel.class, context);

        for (TaskAttempt attempt : p.getAttemptsList()) {
            attempts.add(LHSerializable.fromProto(attempt, TaskAttemptModel.class, context));
        }
        for (VarNameAndVal v : p.getInputVariablesList()) {
            inputVariables.add(LHSerializable.fromProto(v, VarNameAndValModel.class, context));
        }
        simpleTotalAttempts = p.getTotalAttempts();

        if (p.hasExponentialBackoff()) {
            exponentialBackoffRetryPolicy = LHSerializable.fromProto(
                    p.getExponentialBackoff(), ExponentialBackoffRetryPolicyModel.class, context);
        }

        this.executionContext = context;
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    @Override
    public TaskRun.Builder toProto() {
        TaskRun.Builder out = TaskRun.newBuilder()
                .setTaskDefId(taskDefId.toProto())
                .setScheduledAt(LHUtil.fromDate(scheduledAt))
                .setStatus(getStatus())
                .setSource(taskRunSource.toProto())
                .setTimeoutSeconds(timeoutSeconds)
                .setId(id.toProto())
                .setTotalAttempts(simpleTotalAttempts);

        for (VarNameAndValModel v : inputVariables) {
            out.addInputVariables(v.toProto());
        }
        for (TaskAttemptModel attempt : attempts) {
            out.addAttempts(attempt.toProto());
        }
        if (exponentialBackoffRetryPolicy != null) {
            out.setExponentialBackoff(exponentialBackoffRetryPolicy.toProto());
        }

        return out;
    }

    @Override
    public Date getCreatedAt() {
        return scheduledAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(Pair.of("taskDefName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("taskDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("status", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL))
                // NOTE: we're not indexing just based on status because we don't want
                // to have too many reads/writes in RocksDB as those are expensive.
                //
                // Additionally, we could index based on the number of retries, so that
                // we can find all TaskRun's that have been retried. But that maybe can
                // be in the 0.1.1 release, not 0.1.0
                );
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "taskDefName" -> {
                return List.of(new IndexedField(key, this.taskDefId.getName(), TagStorageType.LOCAL));
            }
            case "status" -> {
                return List.of(new IndexedField(key, this.status.toString(), TagStorageType.LOCAL));
            }
        }
        log.warn("Received unknown key for TaskRun Index: {}", key);
        return null;
    }

    @Override
    public TaskRunIdModel getObjectId() {
        return id;
    }

    /**
     * Returns the ID of the WfRun that this TaskRun belongs to.
     * @return the ID of the WfRun that this TaskRun belongs to.
     */
    public WfRunIdModel getWfRunId() {
        return taskRunSource.getSubSource().getWfRunId();
    }

    /**
     * Returns the WfRunModel that this TaskRunModel's TaskRun belongs to.
     * @return the WfRun.
     */
    public WfRunModel getWfRun() {
        return processorContext.getableManager().get(getWfRunId());
    }

    /**
     * Returns the status of this TaskRun, which is always given by the status of the latest
     * attempt.
     * @return
     */
    public TaskStatus getStatus() {
        return getLatestAttempt().getStatus();
    }

    /**
     * Returns whether the TaskRun is still running (that means that a TaskAttempt is currently running
     * OR that there are TaskAttempts that are scheduled or will be scheduled once the retry backoff
     * policy matures)
     * @return whether the TaskRun is still running
     */
    public boolean isStillRunning() {
        switch (status) {
            case TASK_EXCEPTION:
            case TASK_FAILED:
            case TASK_INPUT_VAR_SUB_ERROR:
            case TASK_OUTPUT_SERDE_ERROR:
            case TASK_SUCCESS:
            case TASK_TIMEOUT:
                return false;
            case TASK_SCHEDULED:
            case TASK_RUNNING:
            case TASK_PENDING:
            case UNRECOGNIZED:
        }
        return true;
    }

    public static TaskRunModel fromProto(TaskRun proto, ExecutionContext context) {
        TaskRunModel out = new TaskRunModel();
        out.initFrom(proto, context);
        return out;
    }

    public TaskAttemptModel getLatestAttempt() {
        if (attempts.isEmpty()) return null;
        return attempts.get(attempts.size() - 1);
    }

    public void onTaskAttemptStarted(TaskClaimEvent se) {
        transitionTo(TaskStatus.TASK_RUNNING);

        // create a timer to mark the task is timeout if it does not finish
        ReportTaskRunModel taskResult = new ReportTaskRunModel();
        taskResult.setTaskRunId(id);
        taskResult.setTime(new Date(System.currentTimeMillis() + (1000 * timeoutSeconds)));
        taskResult.setStatus(TaskStatus.TASK_TIMEOUT);
        CommandModel timerCommand = new CommandModel(taskResult, taskResult.getTime());
        LHTimer timer = new LHTimer(timerCommand);
        processorContext.getTaskManager().scheduleTimer(timer);

        // Now that that's out of the way, we can mark the TaskRun as running.
        // Also we need to save the task worker version and client id.
        TaskAttemptModel attempt = getLatestAttempt();
        attempt.setTaskWorkerId(se.getTaskWorkerId());
        attempt.setTaskWorkerVersion(se.getTaskWorkerVersion());
        attempt.setStartTime(se.getTime());
        attempt.setStatus(TaskStatus.TASK_RUNNING);
    }

    public void onTaskAttemptResultReported(ReportTaskRunModel ce) {
        if (ce.getAttemptNumber() >= attempts.size()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Specified Task Attempt does not exist!");
        }

        TaskAttemptModel attempt = attempts.get(ce.getAttemptNumber());

        if (attempt.getStatus() != TaskStatus.TASK_RUNNING) {
            // The task result has already been processed, so ignore this event.
            // Example:
            // 1. Timer event coming to signify timeout on a task that successfully
            // completed already.
            // 2. For TIMEOUT tasks, it's possible the TASK_COMPLETED event was late.
            // Currently, we don't resurrect the workflow. However, in the future,
            // we may want to do something more with it, but it gets really
            // complicated very quickly from a user semantics perspective.
            log.trace(
                    "Ignored {} TaskRunResult on TaskRun {} attempt no. {} w/status {}",
                    ce.getStatus(),
                    id.getStoreableKey(),
                    ce.getAttemptNumber(),
                    attempt.getStatus());
            return;
        }
        TaskDefModel taskDef = executionContext.metadataManager().get(taskDefId);
        if (taskDef.getSchemaOutput() != null
                && taskDef.getSchemaOutput().getValueDef().isMaskedValue()) {
            attempt.setMaskedValue(true);
        }

        attempt.setEndTime(ce.getTime());
        attempt.setLogOutput(ce.getLogOutput());

        if (ce.getOutput().isDeserializationErrorPresent()) {
            attempt.setError(
                    new LHTaskErrorModel(ce.getOutput().getDeserializationErrorMessage(), LHErrorType.VAR_SUB_ERROR));
            attempt.setStatus(TaskStatus.TASK_OUTPUT_SERDE_ERROR);
            attempt.setOutput(ce.getOutput());
            attempt.setException(ce.getException());
            transitionTo(TaskStatus.TASK_OUTPUT_SERDE_ERROR);
        } else {
            attempt.setOutput(ce.getOutput());
            attempt.setStatus(ce.getStatus());
            attempt.setError(ce.getError());
            attempt.setException(ce.getException());
            if (ce.getException() != null) {
                attempt.setOutput(ce.getException().getContent());
            }

            if (ce.getStatus() == TaskStatus.TASK_SUCCESS) {
                // Tell the WfRun that the TaskRun is done.
                transitionTo(TaskStatus.TASK_SUCCESS);
            } else if (shouldRetry()) {
                scheduleRetryAtAppropriateTime();
            } else {
                transitionTo(ce.getStatus());
            }
        }

        // The WfRun may need to advance.
        processorContext.getableManager().get(getWfRunId()).advance(ce.getTime());
    }

    /**
     * Called when a TASK_PENDING retry attempt is now ready to launch according to the retry backoff
     * policy.
     */
    public void markAttemptReadyToSchedule() {
        dispatchTaskToQueue();
    }

    /**
     * Dispatches the latest TaskAttempt to the server's Task Queues.
     */
    public void dispatchTaskToQueue() {
        if (attempts.isEmpty()) {
            throw new IllegalStateException("can't have empty attempts queue");
        }
        TaskAttemptModel attempt = attempts.get(attempts.size() - 1);
        if (attempt.getStatus() != TaskStatus.TASK_PENDING) {
            throw new IllegalStateException("Cannot schedule task attempt that isn't in PENDING state");
        }

        attempt.setStatus(TaskStatus.TASK_SCHEDULED);
        attempt.setScheduleTime(new Date());

        ScheduledTaskModel scheduledTask = new ScheduledTaskModel();
        scheduledTask.setVariables(inputVariables);
        scheduledTask.setAttemptNumber(attempts.size() - 1);
        scheduledTask.setCreatedAt(new Date());
        scheduledTask.setSource(taskRunSource);
        scheduledTask.setTaskDefId(taskDefId);
        scheduledTask.setTaskRunId(id);

        processorContext.getTaskManager().scheduleTask(scheduledTask);
    }

    private boolean shouldRetry() {
        // Shouldn't look at nodeRun to decide whether to retry task or not.
        TaskAttemptModel latest = getLatestAttempt();
        if (latest == null) {
            // This really shouldn't happen I think
            return false;
        }

        if (latest.getStatus() != TaskStatus.TASK_FAILED && latest.getStatus() != TaskStatus.TASK_TIMEOUT) {
            // Can only retry timeout or task failure.
            return false;
        }

        return simpleTotalAttempts > attempts.size();
    }

    private void scheduleRetryAtAppropriateTime() {
        if (exponentialBackoffRetryPolicy == null) {
            attempts.add(new TaskAttemptModel());
            dispatchTaskToQueue();
            return;
        }

        long delayMs = exponentialBackoffRetryPolicy.calculateDelayForNextAttempt(attempts.size());
        Date maturationTime = new Date(System.currentTimeMillis() + delayMs);
        LHTimer timer = new LHTimer(new CommandModel(new TaskAttemptRetryReadyModel(id), maturationTime));
        processorContext.getTaskManager().scheduleTimer(timer);

        TaskAttemptModel nextAttempt = new TaskAttemptModel();
        attempts.add(nextAttempt);
    }

    private void transitionTo(TaskStatus newStatus) {
        TaskStatus previousStatus = status;
        this.status = newStatus;
        processorContext
                .getableUpdates()
                .dispatch(GetableUpdates.create(
                        taskDefId, processorContext.authorization().tenantId(), previousStatus, newStatus));
    }
}
