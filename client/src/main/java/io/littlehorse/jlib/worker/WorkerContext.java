package io.littlehorse.jlib.worker;

import io.littlehorse.jlib.common.proto.NodeRunIdPb;
import io.littlehorse.jlib.common.proto.ScheduledTaskPb;
import java.util.Date;

/**
 * This class contains runtime information about the specific WfRun and NodeRun that
 * is being executed by the Task Worker. It may optionally be added into the input
 * parameters of your LHTaskMethod, and the Runtime will provision the WorkerContext
 * and pass it into the method.
 */
public class WorkerContext {

    private ScheduledTaskPb scheduledTask;
    private Date scheduleTime;
    private String stderr;

    /**
     * Constructor for internal use by the Task Worker Library.
     * @param scheduledTask is the raw payload for the scheduled task.
     * @param scheduleTime is the time that the task was actually scheduled.
     */
    public WorkerContext(ScheduledTaskPb scheduledTask, Date scheduleTime) {
        this.scheduledTask = scheduledTask;
        this.scheduleTime = scheduleTime;
    }

    /**
     * Returns the Id of the WfRun for the NodeRun that's being executed.
     * @return the Id of the WfRun for the NodeRun that's being executed.
     */
    public String getWfRunId() {
        return scheduledTask.getWfRunId();
    }

    /**
     * Returns the threadRunNumber of the NodeRun that's being executed.
     * @return the threadRunNumber of the NodeRun that's being executed.
     */
    public int getThreadRunNumber() {
        return scheduledTask.getThreadRunNumber();
    }

    /**
     * Returns the NodeRun ID for the Task that was just scheduled.
     * @return a `NodeRunIdPb` protobuf class with the ID from the executed NodeRun.
     */
    public NodeRunIdPb getNodeRunId() {
        return NodeRunIdPb
            .newBuilder()
            .setWfRunId(scheduledTask.getWfRunId())
            .setThreadRunNumber(scheduledTask.getThreadRunNumber())
            .setPosition(scheduledTask.getTaskRunPosition())
            .build();
    }

    /**
     * Returns the attemptNumber of the NodeRun that's being executed. If this is the
     * first attempt, returns zero. If this is the first retry, returns 1, and so on.
     * @return the attempt number of the NodeRun that's being executed.
     */
    public int getAttemptNumber() {
        return scheduledTask.getAttemptNumber();
    }

    /**
     * Returns the Node Run Number of the Task Run that is being executed. Note taht
     * this differs from the Node Run Position.
     *
     * When there is a retry, for example, a new Node Run (with an incremented Node
     * Run Position) is created. But the Task Run Number is the same as the previous
     * Task Run that is being retried.
     * @return the current Task Run Number.
     */
    public int getTaskRunNumber() {
        return scheduledTask.getTaskRunNumber();
    }

    /**
     * Returns the Node Run Position of the Task Run that's being executed.
     * @return The Node Run Position of the Task Run that's being executed.
     */
    public int getTaskRunPosition() {
        return scheduledTask.getTaskRunPosition();
    }

    /**
     * Returns the time at which the task was scheduled by the processor. May be
     * useful in certain customer edge cases, eg. to determine whether it's too
     * late to actually perform an action, when (now() - getScheduledTime()) is
     * above some threshold, etc.
     *
     * @return the time at which the current NodeRun was scheduled.
     */
    public Date getScheduledTime() {
        return scheduleTime;
    }

    /**
     * Provides a way to push data into the log output. Any object may be passed in;
     * its String representation will be appended to the logOutput of this NodeRun.
     * @param thing the Object to log to the NodeRun's logOutput.
     */
    public void log(Object thing) {
        if (thing != null) {
            stderr += thing.toString();
        } else {
            stderr += "null";
        }
    }

    /**
     * Returns the current logOutput.
     * @return the current log output.
     */
    public String getLogOutput() {
        return stderr;
    }

    /**
     * Returns an idempotency key that can be used to make calls to upstream api's
     * idempotent.
     * @return an idempotency key.
     */
    public String getIdempotencyKey() {
        return (
            getWfRunId() +
            "/" +
            getThreadRunNumber() +
            "/" +
            scheduledTask.getTaskRunNumber()
        );
    }
}
