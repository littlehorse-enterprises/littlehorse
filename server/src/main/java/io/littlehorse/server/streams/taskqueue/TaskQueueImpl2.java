package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class TaskQueueImpl2 implements TaskQueue {

    private final String taskDefName;
    private final TaskQueueManager parent;
    private final int capacity;
    private final TenantIdModel tenantId;
    private final Lock lock = new ReentrantLock();
    private final Queue<PollTaskRequestObserver> hungryClients = new LinkedList<>();
    private final String instanceName;
    private final LinkedBlockingQueue<QueueItem> pendingTasks;
    private final AtomicLong counter = new AtomicLong(0);

    public TaskQueueImpl2(String taskDefName, TaskQueueManager parent, int capacity, TenantIdModel tenantId) {
        this.taskDefName = taskDefName;
        this.parent = parent;
        this.capacity = capacity;
        this.tenantId = tenantId;
        this.instanceName = parent.getBackend().getInstanceName();
        this.pendingTasks = new LinkedBlockingQueue<>();
    }

    @Override
    public void onRequestDisconnected(PollTaskRequestObserver disconnectedObserver) {
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

    @Override
    public boolean onTaskScheduled(TaskId streamTaskId, ScheduledTaskModel scheduledTask) {
        PollTaskRequestObserver luckyClient = synchronizedBlock(() -> {
            if (!hungryClients.isEmpty()) {
                if (!pendingTasks.isEmpty()) {
                    throw new RuntimeException("Can't have pending tasks and hungry clients");
                }
                return hungryClients.poll();

            } else {
                pendingTasks.add(new QueueItem(streamTaskId, scheduledTask));
                return null;
            }
        });
        if (luckyClient != null) {
            parent.itsAMatch(scheduledTask, luckyClient);
        }
        return false;
    }

    @Override
    public void onPollRequest(PollTaskRequestObserver requestObserver, RequestExecutionContext requestContext) {
        ScheduledTaskModel nextTask = synchronizedBlock(() -> {
            QueueItem nextItem = pendingTasks.poll();
            if (nextItem != null) {
                return nextItem.resolveTask(requestContext);
            } else {
                hungryClients.add(requestObserver);
                return null;
            }
        });
        if (nextTask != null) {
            parent.itsAMatch(nextTask, requestObserver);
        }
    }

    @Override
    public long size() {
        return pendingTasks.size();
    }

    @Override
    public long rehydratedCount() {
        return 0;
    }

    @Override
    public void drainPartition(TaskId partitionToDrain) {
        pendingTasks.removeIf(queueItem -> queueItem.taskId.equals(partitionToDrain));
    }

    @Override
    public TenantIdModel tenantId() {
        return tenantId;
    }

    @Override
    public String taskDefName() {
        return taskDefName;
    }

    private class QueueItem {
        private final TaskId taskId;
        private final ScheduledTaskModel scheduledTask;
        private final String scheduledTaskStoreKey;

        public QueueItem(TaskId streamsTaskId, ScheduledTaskModel scheduledTask) {
            this.taskId = streamsTaskId;
            if (counter.get() > capacity) {
                this.scheduledTask = null;
            } else {
                this.scheduledTask = scheduledTask;
                counter.incrementAndGet();
            }
            this.scheduledTaskStoreKey = scheduledTask.getTaskRunId().toString();
        }

        private ScheduledTaskModel resolveTask(RequestExecutionContext context) {
            if (scheduledTask != null) {
                counter.decrementAndGet();
                return scheduledTask;
            } else {
                return context.getableManager(taskId).getScheduledTask(scheduledTaskStoreKey);
            }
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
