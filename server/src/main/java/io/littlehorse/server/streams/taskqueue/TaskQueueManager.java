package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.CommandSender;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class TaskQueueManager {

    private final ConcurrentHashMap<TenantTaskName, OneTaskQueue> taskQueues;

    private final int individualQueueConfiguredCapacity;
    private final CommandSender commandSender;
    private final String instanceName;
    private final ExecutorService networkThreads;

    public TaskQueueManager(
            String instanceName,
            CommandSender commandSender,
            ExecutorService networkThreads,
            int individualQueueConfiguredCapacity) {
        this.taskQueues = new ConcurrentHashMap<>();
        this.individualQueueConfiguredCapacity = individualQueueConfiguredCapacity;
        this.commandSender = commandSender;
        this.instanceName = instanceName;
        this.networkThreads = networkThreads;
    }

    public void onPollRequest(
            PollTaskRequestObserver listener, TenantIdModel tenantId, RequestExecutionContext requestContext) {
        try {
            OneTaskQueue subQueue = getSubQueue(new TenantTaskName(tenantId, listener.getTaskDefId()));
            subQueue.onPollRequest(listener, requestContext).get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Queue got locked for more than 3 seconds", e);
        }
    }

    public void onRequestDisconnected(PollTaskRequestObserver observer, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, observer.getTaskDefId())).onRequestDisconnected(observer);
    }

    public void onTaskScheduled(
            TaskId streamsTaskId, TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask, TenantIdModel tenantId) {
        getSubQueue(new TenantTaskName(tenantId, taskDef.getName()))
                .onTaskScheduled(streamsTaskId, scheduledTask, networkThreads);
    }

    public void drainPartition(TaskId partitionToDrain) {
        taskQueues.values().forEach(oneTaskQueue -> oneTaskQueue.drainPartition(partitionToDrain));
    }

    public CompletableFuture<RecordMetadata> itsAMatch(
            ScheduledTaskModel scheduledTask, PollTaskRequestObserver luckyClient) {
        TaskClaimEvent taskClaimEvent =
                new TaskClaimEvent(scheduledTask, luckyClient.getClientId(), luckyClient.getTaskWorkerVersion());
        return commandSender.doSend(
                taskClaimEvent, luckyClient.getFreshExecutionContext().authorization());
    }

    private OneTaskQueue getSubQueue(TenantTaskName tenantTask) {
        return taskQueues.computeIfAbsent(
                tenantTask,
                taskToCreate -> new OneTaskQueue(
                        taskToCreate.taskDefName(),
                        this,
                        instanceName,
                        individualQueueConfiguredCapacity,
                        taskToCreate.tenantId()));
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
