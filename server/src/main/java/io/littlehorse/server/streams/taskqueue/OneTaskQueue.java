package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

// One instance of this class is responsible for coordinating the grpc backend for
// one specific TaskDef on one LH Server host.
@Slf4j
public class OneTaskQueue {

    @Getter
    private final String taskDefName;

    private final TaskQueueManager parent;

    @Getter
    private final TenantIdModel tenantId;

    private final Lock lock = new ReentrantLock();
    private final Queue<PollTaskRequestObserver> hungryClients = new LinkedList<>();
    private final String instanceName;
    private final ArrayDeque<String> pendingTaskPartitions;
    private final ArrayDeque<String> pendingTaskIds;

    public OneTaskQueue(String taskDefName, TaskQueueManager parent, TenantIdModel tenantId) {
        this.taskDefName = taskDefName;
        this.parent = parent;
        this.tenantId = tenantId;
        this.instanceName = parent.getBackend().getInstanceName();
        this.pendingTaskPartitions = new ArrayDeque<>();
        this.pendingTaskIds = new ArrayDeque<>();
    }

    /**
     * Called when a gRPC client (and its StreamObserver) disconnect, whether due to
     * a clean
     * shutdown (onCompleted()) or connection error (onError()).
     *
     * @param observer is the TaskQueueStreamObserver for the client whose
     *                 connection is now gone.
     */
    public void onRequestDisconnected(PollTaskRequestObserver disconnectedObserver) {
        // Remove the request listener when the gRPC stream is completed (i.e.
        // graceful shutdown) or when the connection is broken.
        synchronizedBlock(() -> {
            hungryClients.removeIf(thing -> {
                log.trace(
                        "Instance {}: Removing task queue observer for taskdef {} with" + " client id {}: {}",
                        instanceName,
                        taskDefName,
                        disconnectedObserver.getClientId(),
                        disconnectedObserver);
                return thing.equals(disconnectedObserver);
            });
        });
    }

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
     */
    public void onTaskScheduled(TaskId taskId, TaskRunIdModel scheduledTask) {
        // There's two cases here:
        // 1. There are clients waiting for requests, in which case we know that
        // the pendingTaskIds queue/list must be empty.
        // 2. There are no clients waiting for requests. In this case, we just
        // add the task id to the taskid list.
        log.trace(
                "Instance {}: Task scheduled for wfRun {}, queue is empty? {}",
                instanceName,
                scheduledTask.getWfRunId(),
                hungryClients.isEmpty());

        PollTaskRequestObserver luckyClient = synchronizedBlock(() -> {
            if (!hungryClients.isEmpty()) {
                if (!pendingTaskIds.isEmpty()) {
                    throw new RuntimeException("Can't have pending tasks and hungry clients");
                }
                return hungryClients.poll();

            } else {
                pendingTaskPartitions.add(taskId.toString().intern());
                pendingTaskIds.add(scheduledTask.toString());
                return null;
            }
        });
        if (luckyClient != null) {
            parent.itsAMatch(scheduledTask, luckyClient);
        }
    }

    /**
     * Called when a grpc client sends a new PollTaskPb.
     *
     * @param requestObserver is the grpc StreamObserver representing the channel
     *                        that talks to the
     *                        client who made the PollTaskRequest.
     */
    public void onPollRequest(PollTaskRequestObserver requestObserver, RequestExecutionContext requestContext) {

        String nextItem = synchronizedBlock(() -> {
            if (pendingTaskIds.isEmpty()) {
                hungryClients.add(requestObserver);
                return null;
            }
            pendingTaskPartitions.removeFirst();
            return pendingTaskIds.removeFirst();
        });
        if (nextItem != null) {
            parent.itsAMatch(
                    (TaskRunIdModel) TaskRunIdModel.fromString(nextItem, TaskRunIdModel.class), requestObserver);
        }
    }

    public void drainPartition(TaskId partitionToDrain) {
        String partitionStr = partitionToDrain.toString();
        synchronizedBlock(() -> {
            Iterator<String> partIt = pendingTaskPartitions.iterator();
            Iterator<String> idIt = pendingTaskIds.iterator();
            while (partIt.hasNext()) {
                String partition = partIt.next();
                idIt.next();
                if (partition.equals(partitionStr)) {
                    partIt.remove();
                    idIt.remove();
                }
            }
        });
    }

    public long size() {
        return pendingTaskIds.size();
    }

    private void synchronizedBlock(Runnable runnable) {
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    private <T> T synchronizedBlock(Supplier<T> runnable) {
        try {
            lock.lock();
            return runnable.get();
        } finally {
            lock.unlock();
        }
    }
}
