package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TaskQueueManager {

    private final ConcurrentHashMap<TenantTaskName, OneTaskQueue> taskQueues;
    public KafkaStreamsServerImpl backend;

    public TaskQueueManager(KafkaStreamsServerImpl backend) {
        this.taskQueues = new ConcurrentHashMap<>();
        this.backend = backend;
    }

    public void onPollRequest(PollTaskRequestObserver listener, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, listener.getTaskDefId())).onPollRequest(listener);
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
                tenantTask, taskToCreate -> new OneTaskQueue(taskToCreate.taskDefName(), this));
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
