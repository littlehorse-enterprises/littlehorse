package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.TaskId;

@Slf4j
public class AsyncWaiters {

    private final ConcurrentHashMap<String, FutureResponseAndWaitingClient> responses = new ConcurrentHashMap<>();
    private final Map<WorkflowEventDefIdAndTenant, CompletableFuture<WorkflowEvent>> workflowEvents =
            new ConcurrentHashMap<>();
    private final long removeCompletedAfter;
    private final TimeUnit timeUnit;

    public AsyncWaiters() {
        this(LHConstants.MAX_INCOMING_REQUEST_IDLE_TIME);
    }

    public AsyncWaiters(Duration removeCompletedAfter) {
        this.removeCompletedAfter = removeCompletedAfter.toMillis();
        this.timeUnit = TimeUnit.MILLISECONDS;
    }

    public CompletableFuture<Message> getOrRegisterFuture(
            String commandId, Class<?> responseType, CompletableFuture<Message> completable) {
        return getOrRegisterFutureAndWaitingClient(commandId, responseType, completable)
                .completable();
    }

    public void removeCommand(String commandId) {
        FutureResponseAndWaitingClient futureResponse =
                getOrRegisterFutureAndWaitingClient(commandId, Message.class, new CompletableFuture<>());
        futureResponse.waitingClient().complete(null);
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

    public CompletableFuture<WorkflowEvent> getOrRegisterFuture(
            TenantIdModel tenantId, WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId) {
        return getOrRegisterFuture(wfRunId, eventDefId, tenantId, new CompletableFuture<>());
    }

    public CompletableFuture<WorkflowEvent>[] getOrRegisterFuture(
            TenantIdModel tenantId, WfRunIdModel wfRunId, Collection<WorkflowEventDefIdModel> eventDefIds) {
        return getOrRegisterFuture(tenantId, wfRunId, eventDefIds.toArray(WorkflowEventDefIdModel[]::new));
    }

    public void handleRebalance(Set<TaskId> taskIds) {
        // TODO: handle rebalance
    }

    private FutureResponseAndWaitingClient createFuture(String commandId, CompletableFuture<Message> completable) {
        Objects.requireNonNull(commandId);
        Objects.requireNonNull(completable);
        CompletableFuture<FutureResponseAndWaitingClient> waitingClient = new CompletableFuture<>()
                .orTimeout(removeCompletedAfter, timeUnit)
                .handle((o, throwable) -> {
                    return responses.remove(commandId);
                });
        return new FutureResponseAndWaitingClient(completable, waitingClient);
    }

    private FutureResponseAndWaitingClient getOrRegisterFutureAndWaitingClient(
            String commandId, Class<?> responseType, CompletableFuture<Message> completable) {
        Objects.requireNonNull(commandId);
        Objects.requireNonNull(completable);
        return responses.computeIfAbsent(commandId, s -> createFuture(commandId, completable));
    }

    private record WorkflowEventDefIdAndTenant(
            WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId, TenantIdModel tenant) {}

    private record FutureResponseAndWaitingClient(
            CompletableFuture<Message> completable, CompletableFuture<FutureResponseAndWaitingClient> waitingClient) {}
}
