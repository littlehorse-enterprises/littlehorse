package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
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
    private final int capacity;

    @Getter
    private final TenantIdModel tenantId;

    private final Lock lock = new ReentrantLock();
    private final Queue<PollTaskRequestObserver> hungryClients = new LinkedList<>();
    private final String instanceName;
    private final LinkedHashSet<QueueItem> pendingTasks;
    private final AtomicLong numberOfInMemoryTasks = new AtomicLong(0);

    public OneTaskQueue(
            String taskDefName, TaskQueueManager parent, String instanceName, int capacity, TenantIdModel tenantId) {
        this.taskDefName = taskDefName;
        this.parent = parent;
        this.capacity = capacity;
        this.tenantId = tenantId;
        this.instanceName = instanceName;
        this.pendingTasks = new LinkedHashSet<>();
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
    public void onTaskScheduled(TaskId taskId, ScheduledTaskModel scheduledTask, ExecutorService networkThreads) {
        // There's two cases here:
        // 1. There are clients waiting for requests, in which case we know that
        // the pendingTaskIds queue/list must be empty.
        // 2. There are no clients waiting for requests. In this case, we just
        // add the task id to the taskid list.
        log.trace(
                "Instance {}: Task scheduled for wfRun {}, queue is empty? {}",
                instanceName,
                LHLibUtil.getWfRunId(scheduledTask.getSource().toProto()),
                hungryClients.isEmpty());

        PollTaskRequestObserver luckyClient = synchronizedBlock(() -> {
            if (!hungryClients.isEmpty()) {
                if (!pendingTasks.isEmpty()) {
                    throw new RuntimeException("Can't have pending tasks and hungry clients");
                }
                return hungryClients.poll();

            } else {
                pendingTasks.add(new QueueItem(taskId, scheduledTask));
                return null;
            }
        });
        if (luckyClient != null) {
            parent.itsAMatch(scheduledTask, luckyClient)
                    .whenCompleteAsync(
                            (empty, throwable) -> {
                                if (throwable == null) {
                                    luckyClient.sendResponse(scheduledTask);
                                } else {
                                    log.warn("Error sending response to client", throwable);
                                    luckyClient.getResponseObserver().onError(throwable);
                                }
                            },
                            networkThreads);
        }
    }

    /**
     * Called when a grpc client sends a new PollTaskPb.
     *
     * @param requestObserver is the grpc StreamObserver representing the channel
     *                        that talks to the
     *                        client who made the PollTaskRequest.
     */
    public CompletableFuture<Void> onPollRequest(
            PollTaskRequestObserver requestObserver, RequestExecutionContext requestContext) {
        return CompletableFuture.runAsync(() -> {
            QueueItem nextItem = synchronizedBlock(() -> {
                if (pendingTasks.isEmpty()) {
                    hungryClients.add(requestObserver);
                    return null;
                }
                return pendingTasks.removeFirst();
            });
            if (nextItem != null) {
                ScheduledTaskModel toExecute = nextItem.resolveTask(requestContext);
                parent.itsAMatch(toExecute, requestObserver).join();
                requestObserver.sendResponse(toExecute);
            }
        });
    }

    public void drainPartition(TaskId partitionToDrain) {
        synchronizedBlock(() -> {
            pendingTasks.removeIf(queueItem -> queueItem.taskId.equals(partitionToDrain));
        });
    }

    public long size() {
        return pendingTasks.size();
    }

    private class QueueItem {
        private final TaskId taskId;
        private final ScheduledTaskModel scheduledTask;
        private final String scheduledTaskStoreKey;

        public QueueItem(TaskId streamsTaskId, ScheduledTaskModel scheduledTask) {
            this.taskId = streamsTaskId;
            if (numberOfInMemoryTasks.get() > capacity) {
                this.scheduledTask = null;
            } else {
                this.scheduledTask = scheduledTask;
                numberOfInMemoryTasks.incrementAndGet();
            }
            this.scheduledTaskStoreKey = scheduledTask.getTaskRunId().toString();
        }

        private ScheduledTaskModel resolveTask(RequestExecutionContext context) {
            if (scheduledTask != null) {
                numberOfInMemoryTasks.decrementAndGet();
                return scheduledTask;
            } else {
                ScheduledTaskModel task = context.getableManager(taskId).getScheduledTask(scheduledTaskStoreKey);
                if (task == null) {
                    throw new RuntimeException("Unable to find scheduled task for " + scheduledTaskStoreKey);
                }
                return task;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            QueueItem queueItem = (QueueItem) o;
            return Objects.equals(scheduledTaskStoreKey, queueItem.scheduledTaskStoreKey);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(scheduledTaskStoreKey);
        }
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
