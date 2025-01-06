package io.littlehorse.server.streams.taskqueue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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
    private final Map<TaskId, LinkedBlockingQueue<QueueItem>> pendingTasksPerPartition;
    private final Map<TaskId, Boolean> rehydrationPerPartition = new ConcurrentHashMap<>();
    private final AtomicReference<Iterator<TaskId>> partitionIterator;
    private final AtomicLong counter = new AtomicLong();

    public TaskQueueImpl(String taskDefName, TaskQueueManager parent, int capacity, TenantIdModel tenantId) {
        this.taskDefName = taskDefName;
        this.parent = parent;
        this.capacity = capacity;
        this.tenantId = tenantId;
        this.instanceName = parent.getBackend().getInstanceName();
        this.pendingTasksPerPartition = new ConcurrentHashMap<>();
        this.partitionIterator = new AtomicReference<>(
                Iterables.cycle(pendingTasksPerPartition.keySet()).iterator());
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
            if (rehydrationPerPartition.getOrDefault(streamTaskId, false)) {
                return true;
            }
            LinkedBlockingQueue<QueueItem> pendingTasks =
                    pendingTasksPerPartition.getOrDefault(streamTaskId, new LinkedBlockingQueue<>(capacity));
            boolean added = pendingTasks.offer(new QueueItem(streamTaskId, scheduledTask));
            if (added) {
                pendingTasksPerPartition.computeIfAbsent(streamTaskId, taskId -> {
                    partitionIterator.set(
                            Iterables.cycle(Sets.union(pendingTasksPerPartition.keySet(), Set.of(streamTaskId)))
                                    .iterator());
                    return pendingTasks;
                });
                pendingTasksPerPartition.putIfAbsent(streamTaskId, pendingTasks);
            } else {
                rehydrationPerPartition.put(streamTaskId, true);
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
            if (partitionIterator.get().hasNext()) {
                QueueItem nextItem = pendingTasksPerPartition
                        .get(partitionIterator.get().next())
                        .poll();
                if (nextItem != null) {
                    parent.itsAMatch(nextItem.scheduledTask(), requestObserver);
                }
                return;
            }
            hungryClients.add(requestObserver);
        });
    }

    @Override
    public long size() {
        return pendingTasksPerPartition.values().stream()
                .map(LinkedBlockingQueue::size)
                .reduce(0, Integer::sum);
    }

    @Override
    public long rehydratedCount() {
        return 0;
    }

    @Override
    public void drainPartition(TaskId partitionToDrain) {
        pendingTasksPerPartition.put(partitionToDrain, new LinkedBlockingQueue<>());
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
