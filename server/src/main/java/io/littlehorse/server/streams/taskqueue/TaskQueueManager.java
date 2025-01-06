package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.io.Closeable;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class TaskQueueManager implements Closeable {

    private final ConcurrentHashMap<TenantTaskName, TaskQueue> taskQueues;

    @Getter
    private LHServer backend;

    private final int individualQueueConfiguredCapacity;
    private final TaskQueueCommandProducer taskQueueCommandProducer;

    public TaskQueueManager(
            LHServer backend,
            int individualQueueConfiguredCapacity,
            TaskQueueCommandProducer taskQueueCommandProducer) {
        this.taskQueues = new ConcurrentHashMap<>();
        this.backend = backend;
        this.individualQueueConfiguredCapacity = individualQueueConfiguredCapacity;
        this.taskQueueCommandProducer = taskQueueCommandProducer;
    }

    public void onPollRequest(
            PollTaskRequestObserver listener, TenantIdModel tenantId, RequestExecutionContext requestContext) {
        getSubQueue(new TenantTaskName(tenantId, listener.getTaskDefId())).onPollRequest(listener, requestContext);
    }

    public void onRequestDisconnected(PollTaskRequestObserver observer, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, observer.getTaskDefId())).onRequestDisconnected(observer);
    }

    public void onTaskScheduled(
            TaskId streamsTaskId, TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, taskDef.getName())).onTaskScheduled(streamsTaskId, scheduledTask);
    }

    public void drainPartition(TaskId partitionToDrain) {
        taskQueues.values().forEach(oneTaskQueue -> oneTaskQueue.drainPartition(partitionToDrain));
    }

    public void itsAMatch(ScheduledTaskModel scheduledTask, PollTaskRequestObserver luckyClient) {
        taskQueueCommandProducer.returnTaskToClient(scheduledTask, luckyClient);
    }

    private TaskQueue getSubQueue(TenantTaskName tenantTask) {
        return taskQueues.computeIfAbsent(
                tenantTask,
                taskToCreate -> new TaskQueueImpl2(
                        taskToCreate.taskDefName(), this, individualQueueConfiguredCapacity, taskToCreate.tenantId()));
    }

    public Collection<TaskQueue> all() {
        return taskQueues.values();
    }

    public long rehydrationCount() {
        return taskQueues.values().stream()
                .mapToLong(queue -> queue.rehydratedCount())
                .sum();
    }

    @Override
    public void close() {
        taskQueueCommandProducer.close();
    }

    private record TenantTaskName(TenantIdModel tenantId, String taskDefName) {

        public TenantTaskName {
            Objects.requireNonNull(tenantId.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TenantTaskName that)) return false;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(taskDefName, that.taskDefName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, taskDefName);
        }
    }
}
