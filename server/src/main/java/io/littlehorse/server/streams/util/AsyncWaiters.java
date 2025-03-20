package io.littlehorse.server.streams.util;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.InternalWaitForWfEventRequest;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.kafka.streams.processor.TaskId;

public class AsyncWaiters {

    private final ConcurrentHashMap<String, CommandWaiter> commandWaiters;
    private final HashMap<WfRunIdModel, GroupOfObserversWaitingForEvent> eventWaiters;
    private final Lock eventWaiterLock = new ReentrantLock();

    public AsyncWaiters() {
        commandWaiters = new ConcurrentHashMap<>();
        eventWaiters = new HashMap<>();
        ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(2);
        scheduledService.scheduleAtFixedRate(this::cleanupOldCommandWaiters, 0, 10, TimeUnit.SECONDS);
        scheduledService.scheduleAtFixedRate(this::cleanupOldWorkflowEventWaiters, 1, 10, TimeUnit.SECONDS);
    }

    public void registerObserverWaitingForCommand(
            String commandId, int partition, StreamObserver<WaitForCommandResponse> observer) {
        CommandWaiter tmp = new CommandWaiter(commandId, partition);
        CommandWaiter waiter = commandWaiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setObserverAndMaybeComplete(observer)) {
            commandWaiters.remove(commandId);
        }
    }

    public void markCommandFailed(String commandId, Throwable exception) {
        CommandWaiter tmp = new CommandWaiter(commandId, -1);
        CommandWaiter waiter = commandWaiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setExceptionAndMaybeComplete(exception)) {
            commandWaiters.remove(commandId);
        }
    }

    public void registerCommandProcessed(String commandId, WaitForCommandResponse response) {
        CommandWaiter tmp = new CommandWaiter(commandId, -1);
        CommandWaiter waiter = commandWaiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setResponseAndMaybeComplete(response)) {
            commandWaiters.remove(commandId);
        }
    }

    public void registerWorkflowEventHappened(WorkflowEventModel event) {
        WfRunIdModel key = event.getId().getWfRunId();

        try {
            eventWaiterLock.lock();
            GroupOfObserversWaitingForEvent group = eventWaiters.get(key);
            if (group != null) {
                if (group.completeWithEvent(event)) {
                    eventWaiters.remove(key);
                }
            }
        } finally {
            eventWaiterLock.unlock();
        }
    }

    public void registerObserverWaitingForWorkflowEvent(
            InternalWaitForWfEventRequest req, StreamObserver<WorkflowEvent> observer, RequestExecutionContext ctx) {

        WfRunIdModel wfRunId = LHSerializable.fromProto(req.getRequest().getWfRunId(), WfRunIdModel.class, ctx);

        try {
            eventWaiterLock.lock();
            GroupOfObserversWaitingForEvent tmp = new GroupOfObserversWaitingForEvent();
            GroupOfObserversWaitingForEvent group = eventWaiters.putIfAbsent(wfRunId, tmp);
            if (group == null) group = tmp;

            group.addObserverForWorkflowEvent(req.getRequest(), observer, ctx);

            // Now iterate through all WorkflowEvents for that wfRun.
            for (WorkflowEventModel candidate : ctx.getableManager().getWorkflowEvents(wfRunId)) {
                if (group.completeWithEvent(candidate)) {
                    eventWaiters.remove(wfRunId);
                    break;
                }
            }
        } finally {
            eventWaiterLock.unlock();
        }
    }

    public void handleRebalance(Set<TaskId> assignedTasks) {
        Set<Integer> assignedPartitions =
                assignedTasks.stream().map(TaskId::partition).collect(Collectors.toSet());

        commandWaiters.values().stream()
                .filter(commandWaiter -> !assignedPartitions.contains(commandWaiter.getCommandPartition()))
                .forEach(migratedCommandWaiter -> migratedCommandWaiter.handleMigration());
    }

    private void cleanupOldCommandWaiters() {
        Iterator<Map.Entry<String, CommandWaiter>> iter =
                commandWaiters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, CommandWaiter> pair = iter.next();
            Duration timePassed = Duration.between(
                    Instant.now(), pair.getValue().getArrivalTime().toInstant());
            if (timePassed.compareTo(LHConstants.MAX_INCOMING_REQUEST_IDLE_TIME) > 0) {
                CommandWaiter waiter = pair.getValue();
                if (waiter.getObserver() != null) {
                    waiter.getObserver()
                            .onError(new StatusRuntimeException(Status.DEADLINE_EXCEEDED.withDescription(
                                    "Command not processed within deadline: likely due to rebalance")));
                }
                iter.remove();
            }
        }
    }

    private void cleanupOldWorkflowEventWaiters() {
        try {
            eventWaiterLock.lock();
            Iterator<Map.Entry<WfRunIdModel, GroupOfObserversWaitingForEvent>> waitForEventIter =
                    eventWaiters.entrySet().iterator();
            while (waitForEventIter.hasNext()) {
                Map.Entry<WfRunIdModel, GroupOfObserversWaitingForEvent> entry = waitForEventIter.next();
                if (entry.getValue().cleanupOldWaitersAndCheckIfEmpty()) {
                    waitForEventIter.remove();
                }
            }
        } finally {
            eventWaiterLock.unlock();
        }
    }

    private static class GroupOfObserversWaitingForEvent {

        private final List<WorkflowEventWaiter> waitingRequests;

        public GroupOfObserversWaitingForEvent() {
            this.waitingRequests = new ArrayList<>();
        }

        public boolean completeWithEvent(WorkflowEventModel event) {
            Iterator<WorkflowEventWaiter> iter = waitingRequests.iterator();
            while (iter.hasNext()) {
                WorkflowEventWaiter waiter = iter.next();
                if (waiter.maybeComplete(event)) {
                    iter.remove();
                }
            }
            return waitingRequests.isEmpty();
        }

        public void addObserverForWorkflowEvent(
                AwaitWorkflowEventRequest request,
                StreamObserver<WorkflowEvent> observer,
                RequestExecutionContext context) {
            waitingRequests.add(new WorkflowEventWaiter(request, observer, context));
        }

        public boolean cleanupOldWaitersAndCheckIfEmpty() {
            waitingRequests.removeIf(WorkflowEventWaiter::maybeExpire);
            return waitingRequests.isEmpty();
        }
    }
}
