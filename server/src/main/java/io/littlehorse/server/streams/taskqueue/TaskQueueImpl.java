package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class TaskQueueImpl implements TaskQueue {

    private final String taskDefName;
    private final TaskQueueManager parent;
    private final int capacity;
    private final TenantIdModel tenantId;
    private final Lock lock = new ReentrantLock();
    private final Queue<PollTaskRequestObserver> hungryClients = new LinkedList<>();
    private final String instanceName;
    private final LinkedBlockingQueue<QueueItem> pendingTasks;
    private final AtomicBoolean needsRehydration = new AtomicBoolean(false);

    public TaskQueueImpl(String taskDefName, TaskQueueManager parent, int capacity, TenantIdModel tenantId) {
        this.taskDefName = taskDefName;
        this.parent = parent;
        this.capacity = capacity;
        this.tenantId = tenantId;
        this.instanceName = parent.getBackend().getInstanceName();
        this.pendingTasks = new LinkedBlockingQueue<>(capacity);
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
        boolean outOfCapacity = synchronizedBlock(() -> {
            if (needsRehydration.get()) {
                return true;
            }
            boolean added = pendingTasks.offer(new QueueItem(streamTaskId, scheduledTask));
            if (!added) {
                needsRehydration.set(true);
            }
            return !added;
        });
        if (!outOfCapacity && !hungryClients.isEmpty()) {
            synchronizedBlock(() -> {
                PollTaskRequestObserver hungryClient = hungryClients.poll();
                if (hungryClient != null) {
                    parent.itsAMatch(scheduledTask, hungryClient);
                }
            });
        }
        return outOfCapacity;
    }

    @Override
    public void onPollRequest(PollTaskRequestObserver requestObserver, RequestExecutionContext requestContext) {
        synchronizedBlock(() -> {
            QueueItem nextItem = pendingTasks.poll();
            if (nextItem != null) {
                parent.itsAMatch(nextItem.scheduledTask(), requestObserver);
            } else {
                hungryClients.add(requestObserver);
            }
        });
    }

    @Override
    public int size() {
        return pendingTasks.size();
    }

    @Override
    public long rehydratedCount() {
        return 0;
    }

    @Override
    public void drainPartition(TaskId partitionToDrain) {
        pendingTasks.removeIf(queueItem -> queueItem.streamsTaskId().equals(partitionToDrain));
    }

    @Override
    public TenantIdModel tenantId() {
        return tenantId;
    }

    @Override
    public String taskDefName() {
        return taskDefName;
    }

    private record QueueItem(TaskId streamsTaskId, ScheduledTaskModel scheduledTask) {}

    private void synchronizedBlock(Runnable runnable) {
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    private boolean synchronizedBlock(Supplier<Boolean> booleanSupplier) {
        try {
            lock.lock();
            return booleanSupplier.get();
        } finally {
            lock.unlock();
        }
    }
}
