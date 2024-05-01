package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

public class TaskQueueManager {

    private final ConcurrentHashMap<TenantTaskName, OneTaskQueue> taskQueues;

    @Getter
    private KafkaStreamsServerImpl backend;

    private final int individualQueueConfiguredCapacity;

    public TaskQueueManager(KafkaStreamsServerImpl backend, int individualQueueConfiguredCapacity) {
        this.taskQueues = new ConcurrentHashMap<>();
        this.backend = backend;
        this.individualQueueConfiguredCapacity = individualQueueConfiguredCapacity;
    }

    public void onPollRequest(
            PollTaskRequestObserver listener, TenantIdModel tenantId, RequestExecutionContext requestContext) {
        getSubQueue(new TenantTaskName(tenantId, listener.getTaskDefId())).onPollRequest(listener, requestContext);
    }

    public void onRequestDisconnected(PollTaskRequestObserver observer, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, observer.getTaskDefId())).onRequestDisconnected(observer);
    }

    public void onTaskScheduled(TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, taskDef.getName())).onTaskScheduled(scheduledTask);
    }

    public void itsAMatch(ScheduledTaskModel scheduledTask, PollTaskRequestObserver luckyClient) {
        backend.returnTaskToClient(scheduledTask, luckyClient);
    }

    private OneTaskQueue getSubQueue(TenantTaskName tenantTask) {
        return taskQueues.computeIfAbsent(
                tenantTask,
                taskToCreate -> new OneTaskQueue(
                        taskToCreate.taskDefName(), this, individualQueueConfiguredCapacity, taskToCreate.tenantId()));
    }

    public void clear() {
        taskQueues.clear();
    }

    public Collection<OneTaskQueue> all() {
        return taskQueues.values();
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
