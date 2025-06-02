package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.streams.processor.TaskId;

public class AsyncWaiters {

    private final ConcurrentHashMap<String, CompletableFuture<Message>> responses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WorkflowEventDefIdAndTenant, CompletableFuture<WorkflowEvent>> workflowEvents =
            new ConcurrentHashMap<>();

    public AsyncWaiters() {}

    public void put(String commandId, CompletableFuture<Message> completable) {
        responses.put(commandId, completable);
    }

    public CompletableFuture<Message> getOrRegisterFuture(
            String commandId, Class<?> responseType, CompletableFuture<Message> completable) {
        return responses.computeIfAbsent(commandId, s -> completable);
    }

    public CompletableFuture<WorkflowEvent> getOrRegisterFuture(
            WfRunIdModel wfRunId,
            WorkflowEventDefIdModel eventDefId,
            TenantIdModel tenantId,
            CompletableFuture<WorkflowEvent> completableWorkflowEvent) {
        return workflowEvents.computeIfAbsent(
                new WorkflowEventDefIdAndTenant(wfRunId, eventDefId, tenantId), s -> completableWorkflowEvent);
    }

    public CompletableFuture<WorkflowEvent>[] getOrRegisterFuture(
            TenantIdModel tenantId, WfRunIdModel wfRunId, WorkflowEventDefIdModel... eventDefIds) {
        return Arrays.stream(eventDefIds)
                .map(w -> getOrRegisterFuture(wfRunId, w, tenantId, new CompletableFuture<>()))
                .toArray(CompletableFuture[]::new);
    }

    public CompletableFuture<WorkflowEvent>[] getOrRegisterFuture(
            TenantIdModel tenantId, WfRunIdModel wfRunId, Collection<WorkflowEventDefIdModel> eventDefIds) {
        return getOrRegisterFuture(tenantId, wfRunId, eventDefIds.toArray(WorkflowEventDefIdModel[]::new));
    }

    public void completeEvent(
            TenantIdModel tenantId, WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId, WorkflowEvent event) {
        workflowEvents
                .get(new WorkflowEventDefIdAndTenant(wfRunId, eventDefId, tenantId))
                .complete(event);
    }

    public void handleRebalance(Set<TaskId> taskIds) {
        // TODO: handle rebalance
    }

    private record WorkflowEventDefIdAndTenant(
            WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId, TenantIdModel tenant) {}
}
