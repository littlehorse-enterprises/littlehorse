package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncWaiters {

    // private Lock lock;
    private ConcurrentHashMap<String, CommandWaiter> commandWaiters;
    private ConcurrentHashMap<String, WorkflowEventWaiter> eventWaiters;

    private static final long MAX_WAITER_AGE = 1000 * 60;

    public AsyncWaiters() {
        eventWaiters = new ConcurrentHashMap<>();
        commandWaiters = new ConcurrentHashMap<>();
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
        String key = event.getId().toString();
        WorkflowEventWaiter waiter = eventWaiters.get(key);
        if (waiter != null && waiter.completeWithEvent(event)) {
            eventWaiters.remove(key);
        }
    }

    public void registerObserverWaitingForWorkflowEvent(
            WorkflowEventId idProto, StreamObserver<WorkflowEvent> observer, RequestExecutionContext ctx) {
        WorkflowEventIdModel id = WorkflowEventIdModel.fromProto(idProto, WorkflowEventIdModel.class, ctx);
        String key = id.toString();
        WorkflowEventWaiter waiter = new WorkflowEventWaiter(id, observer);
        eventWaiters.put(key, waiter);

        // Now try to get the event out
        WorkflowEventModel event = ctx.getableManager().get(id);
        if (event != null && waiter.completeWithEvent(event)) {
            eventWaiters.remove(key);
        }
    }

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
                        .onError(new RuntimeException(
                                "Request not processed on this worker, likely due to" + " rebalance"));
            }
            iter.remove();
        }
    }
}
