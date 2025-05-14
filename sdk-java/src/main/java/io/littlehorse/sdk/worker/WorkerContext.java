package io.littlehorse.sdk.worker;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunSource;
import io.littlehorse.sdk.common.proto.UserTaskTriggerReference;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.Date;

/**
 * This class contains runtime information about the specific WfRun and NodeRun that is being
 * executed by the Task Worker. It may optionally be added into the input parameters of your
 * LHTaskMethod, and the Runtime will provision the WorkerContext and pass it into the method.
 */
public class WorkerContext {

    private ScheduledTask scheduledTask;

    private Date scheduleTime;
    private String logOutput;

    /**
     * Constructor for internal use by the Task Worker Library.
     *
     * @param scheduledTask is the raw payload for the scheduled task.
     * @param scheduleTime is the time that the task was actually scheduled.
     */
    public WorkerContext(ScheduledTask scheduledTask, Date scheduleTime) {
        this.scheduledTask = scheduledTask;
        this.scheduleTime = scheduleTime;
        this.logOutput = "";
    }

    /**
     * Returns the Id of the WfRun for the NodeRun that's being executed.
     *
     * @return the Id of the WfRun for the NodeRun that's being executed.
     */
    public WfRunId getWfRunId() {
        return LHLibUtil.getWfRunId(scheduledTask.getSource());
    }

    /**
     * Returns the NodeRun ID for the Task that was just scheduled.
     *
     * @return a `NodeRunIdPb` protobuf class with the ID from the executed NodeRun.
     */
    public NodeRunId getNodeRunId() {
        TaskRunSource source = scheduledTask.getSource();
        switch (source.getTaskRunSourceCase()) {
            case TASK_NODE:
                return source.getTaskNode().getNodeRunId();
            case USER_TASK_TRIGGER:
                return source.getUserTaskTrigger().getNodeRunId();
            case TASKRUNSOURCE_NOT_SET:
        }
        return null;
    }

    /**
     * Returns the attemptNumber of the NodeRun that's being executed. If this is the first attempt,
     * returns zero. If this is the first retry, returns 1, and so on.
     *
     * @return the attempt number of the NodeRun that's being executed.
     */
    public int getAttemptNumber() {
        return scheduledTask.getAttemptNumber();
    }

    /**
     * Returns the time at which the task was scheduled by the processor. May be useful in certain
     * customer edge cases, eg. to determine whether it's too late to actually perform an action,
     * when (now() - getScheduledTime()) is above some threshold, etc.
     *
     * @return the time at which the current NodeRun was scheduled.
     */
    public Date getScheduledTime() {
        return scheduleTime;
    }

    /**
     * Provides a way to push data into the log output. Any object may be passed in; its String
     * representation will be appended to the logOutput of this NodeRun.
     *
     * @param thing the Object to log to the NodeRun's logOutput.
     */
    public void log(Object thing) {
        if (thing != null) {
            logOutput += thing.toString();
        } else {
            logOutput += "null";
        }
    }

    /**
     * Returns the current logOutput.
     *
     * @return the current log output.
     */
    public String getLogOutput() {
        return logOutput;
    }

    /**
     * Returns the TaskRunId of this TaskRun.
     * @return the associated TaskRunId.
     */
    public TaskRunId getTaskRunId() {
        return scheduledTask.getTaskRunId();
    }

    private UserTaskTriggerReference getUserTaskTrigger() {
        return scheduledTask.getSource().hasUserTaskTrigger()
                ? scheduledTask.getSource().getUserTaskTrigger()
                : null;
    }

    /**
     * If this TaskRun is a User Task Reminder TaskRun, then this method returns the
     * UserId of the user who the associated UserTask is assigned to. Returns
     * null if:
     * - this TaskRun is not a Reminder Task
     * - this TaskRun is a Reminder Task, but the UserTaskRun does not have an assigned
     *   user id.
     *
     * @return the id of the user that the associated UserTask is assigned to.
     */
    public String getUserId() {
        UserTaskTriggerReference uttr = getUserTaskTrigger();
        if (uttr == null) return null;

        return uttr.hasUserId() ? uttr.getUserId() : null;
    }

    /**
     * If this TaskRun is a User Task Reminder TaskRun, then this method returns the
     * UserGroup that the associated UserTask is assigned to. Returns null if:
     * - this TaskRun is not a Reminder Task
     * - this TaskRun is a Reminder Task, but the UserTaskRun does not have an
     *   associated User Group
     *
     * @return the id of the User Group that the associated UserTask is assigned to.
     */
    public String getUserGroup() {
        UserTaskTriggerReference uttr = getUserTaskTrigger();
        if (uttr == null) return null;

        return uttr.hasUserGroup() ? uttr.getUserGroup() : null;
    }

    /**
     * Returns an idempotency key that can be used to make calls to upstream api's idempotent across
     * TaskRun Retries.
     *
     * @return an idempotency key.
     */
    public String getIdempotencyKey() {
        return LHLibUtil.taskRunIdToString(getTaskRunId());
    }
}
