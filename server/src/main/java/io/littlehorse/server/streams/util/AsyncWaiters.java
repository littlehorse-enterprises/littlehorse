package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.NotImplementedException;

public class AsyncWaiters {

    // private Lock lock;
    private ConcurrentHashMap<String, AsyncWaiter<?>> waiters;

    private static final long MAX_WAITER_AGE = 1000 * 60;

    public AsyncWaiters() {
        // lock = new ReentrantLock();
        waiters = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void registerObserverWaitingForCommand(String commandId, StreamObserver<WaitForCommandResponse> observer) {
        AsyncWaiter<WaitForCommandResponse> tmp = new AsyncWaiter<>(commandId);
        AsyncWaiter<WaitForCommandResponse> waiter = (AsyncWaiter<WaitForCommandResponse>) waiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setObserverAndMaybeComplete(observer)) {
            waiters.remove(commandId);
        }
    }

    @SuppressWarnings("unchecked")
    public void markCommandFailed(String commandId, Exception exception) {
        AsyncWaiter<WaitForCommandResponse> tmp = new AsyncWaiter<>(commandId);
        AsyncWaiter<WaitForCommandResponse> waiter = (AsyncWaiter<WaitForCommandResponse>) waiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setExceptionAndMaybeComplete(exception)) {
            waiters.remove(commandId);
        }
    }

    @SuppressWarnings("unchecked")
    public void registerCommandProcessed(String commandId, WaitForCommandResponse response) {
        AsyncWaiter<WaitForCommandResponse> tmp = new AsyncWaiter<>(commandId);
        AsyncWaiter<WaitForCommandResponse> waiter = (AsyncWaiter<WaitForCommandResponse>) waiters.putIfAbsent(commandId, tmp);
        if (waiter == null) waiter = tmp;
        if (waiter.setResponseAndMaybeComplete(response)) {
            waiters.remove(commandId);
        }
    }

    public void registerWorkflowEventHappened(WorkflowEventModel event) {
        throw new NotImplementedException();
    }

    public void registerObserverWaitingForWorkflowEvent(WorkflowEventId id, StreamObserver<WorkflowEvent> observer) {
        throw new NotImplementedException();
    }

    public void cleanupOldWaiters() {
        Iterator<Map.Entry<String, AsyncWaiter<?>>> iter = waiters.entrySet().iterator();
        long now = System.currentTimeMillis();
        while (iter.hasNext()) {
            Map.Entry<String, AsyncWaiter<?>> pair = iter.next();
            long age = now - pair.getValue().getArrivalTime().getTime();
            if (age < MAX_WAITER_AGE) {
                break;
            }
            AsyncWaiter<?> waiter = pair.getValue();
            if (waiter.getObserver() != null) {
                waiter.getObserver()
                        .onError(new RuntimeException(
                                "Request not processed on this worker, likely due to" + " rebalance"));
            }
            iter.remove();
        }
    }
}
