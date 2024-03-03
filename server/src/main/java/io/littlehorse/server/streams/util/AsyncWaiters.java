package io.littlehorse.server.streams.util;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.InternalWaitForWfEventRequest;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncWaiters {

    private ConcurrentHashMap<String, CommandWaiter> commandWaiters;
    private HashMap<String, GroupOfObserversWaitingForEvent> eventWaiters;
    private Lock eventWaiterLock = new ReentrantLock();

    private static final long MAX_WAITER_AGE = 1000 * 60;

    public AsyncWaiters() {
        commandWaiters = new ConcurrentHashMap<>();
        eventWaiters = new HashMap<>();
    }

    public void registerObserverWaitingForCommand(String commandId, StreamObserver<WaitForCommandResponse> observer) {
        CommandWaiter tmp = new CommandWaiter(commandId);
        CommandWaiter waiter = commandWaiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setObserverAndMaybeComplete(observer)) {
            commandWaiters.remove(commandId);
        }
    }

    public void markCommandFailed(String commandId, Exception exception) {
        CommandWaiter tmp = new CommandWaiter(commandId);
        CommandWaiter waiter = commandWaiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setExceptionAndMaybeComplete(exception)) {
            commandWaiters.remove(commandId);
        }
    }

    public void registerCommandProcessed(String commandId, WaitForCommandResponse response) {
        CommandWaiter tmp = new CommandWaiter(commandId);
        CommandWaiter waiter = commandWaiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setResponseAndMaybeComplete(response)) {
            commandWaiters.remove(commandId);
        }
    }

    public void registerWorkflowEventHappened(WorkflowEventModel event) {
        String key = event.getId().getWfRunId().toString();

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
        String key = wfRunId.toString();

        try {
            eventWaiterLock.lock();
            GroupOfObserversWaitingForEvent tmp = new GroupOfObserversWaitingForEvent();
            GroupOfObserversWaitingForEvent group = eventWaiters.putIfAbsent(key, tmp);
            if (group == null) group = tmp;

            group.addObserverForWorkflowEvent(req.getRequest(), observer, ctx);

            // Now iterate through all WorkflowEvents for that wfRun.
            for (WorkflowEventModel candidate :
                    ctx.getableManager().iterateOverPrefix(wfRunId.toString() + "/", WorkflowEventModel.class)) {
                if (group.completeWithEvent(candidate)) {
                    eventWaiters.remove(key);
                    break;
                }
            }
        } finally {
            eventWaiterLock.unlock();
        }
    }

    // public void registerObserverWaitingForWorkflowEvent(
    //         List<WorkflowEventId> idProtos, StreamObserver<WorkflowEvent> observer, RequestExecutionContext ctx) {

    //     for (WorkflowEventId idProto : idProtos) {
    //         WorkflowEventIdModel id = WorkflowEventIdModel.fromProto(idProto, WorkflowEventIdModel.class, ctx);
    //         String key = id.toString();

    //         try {
    //             GroupOfObserversWaitingForEvent tmp = new GroupOfObserversWaitingForEvent();
    //             GroupOfObserversWaitingForEvent group = eventWaiters.putIfAbsent(key, tmp);
    //             if (group == null) group = tmp;

    //             eventWaiterLock.lock();
    //             group.addObserverForWorkflowEvent(observer);

    //             // Now try to get the event out
    //             WorkflowEventModel event = ctx.getableManager().get(id);
    //             if (event != null && group.completeWithEvent(event)) {
    //                 eventWaiters.remove(key);
    //             }
    //         } finally {
    //             eventWaiterLock.unlock();
    //         }
    //     }
    // }

    public void cleanupOldWaiters() {
        Iterator<Map.Entry<String, CommandWaiter>> iter =
                commandWaiters.entrySet().iterator();
        long now = System.currentTimeMillis();
        while (iter.hasNext()) {
            Map.Entry<String, CommandWaiter> pair = iter.next();
            long age = now - pair.getValue().getArrivalTime().getTime();
            if (age < MAX_WAITER_AGE) {
                break;
            }
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

class GroupOfObserversWaitingForEvent {

    private List<WorkflowEventWaiter> waitingRequests;

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
}
