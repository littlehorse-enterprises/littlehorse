package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import org.apache.kafka.streams.processor.TaskId;

public interface TaskQueue {

    /**
     * Called when a gRPC client (and its StreamObserver) disconnect, whether due to
     * a clean
     * shutdown (onCompleted()) or connection error (onError()).
     *
     * @param disconnectedObserver is the TaskQueueStreamObserver for the client whose
     *                 connection is now gone.
     */
    void onRequestDisconnected(PollTaskRequestObserver disconnectedObserver);

    /**
     * Called in two places: 1. In the CommandProcessorDaoImpl::scheduleTask() 2. In
     * the
     * CommandProcessor::init().
     *
     * <p>
     * Item 1) is quite self-explanatory.
     *
     * <p>
     * For Item 2), remember that the Task Queue Manager system is only in-memory.
     * Upon a restart
     * or rebalance, we need to rebuild that state. During the init() call, we
     * iterate through all
     * currently scheduled but not started tasks in the state store.
     *
     * @param scheduledTask is the ::getObjectId() for the TaskScheduleRequest
     *                        that was just
     *                        scheduled.
     * @return True if the task was successfully scheduled, or False if the queue is full.
     */
    boolean onTaskScheduled(TaskId streamsTaskId, ScheduledTaskModel scheduledTask);

    /**
     * Called when a grpc client sends a new PollTaskPb.
     *
     * @param requestObserver is the grpc StreamObserver representing the channel
     *                        that talks to the
     *                        client who made the PollTaskRequest.
     */
    void onPollRequest(PollTaskRequestObserver requestObserver, RequestExecutionContext requestContext);

    int size();

    long rehydratedCount();

    void drainPartition(TaskId partitionToDrain);

    TenantIdModel tenantId();

    String taskDefName();
}
