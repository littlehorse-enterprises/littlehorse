package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
public class AsyncWaiters {

    private Lock lock;
    private LinkedHashMap<String, AsyncWaiter> waiters;

    private static final long MAX_WAITER_AGE = 1000 * 60;

    public AsyncWaiters() {
        lock = new ReentrantLock();
        waiters = new LinkedHashMap<>();
    }

    public void registerObserverWaitingForCommand(String commandId, StreamObserver<WaitForCommandResponse> observer) {
        try {
            lock.lock();
            AsyncWaiter waiter = waiters.get(commandId);
            if (waiter == null) {
                waiters.put(commandId, new AsyncWaiter(commandId, observer));
            } else {
                if (waiter.getObserver() != null) {
                    // this means the request has come in again...
                    waiter.setObserver(observer);
                    log.warn("Got a retry request before the event was processed");
                } else {
                    waiter.setObserver(observer);
                    waiter.onMatched();
                    waiters.remove(commandId);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void markCommandFailed(String commandId, Exception exception) {
        // This happens when there is an unexpected error in the processing.
        try {
            lock.lock();
            AsyncWaiter waiter = waiters.get(commandId);
            if (waiter == null) {
                waiters.put(commandId, new AsyncWaiter(commandId, exception));
            } else {
                waiter.setCaughtException(exception);
                waiter.onMatched();
                waiters.remove(commandId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void registerCommandProcessed(String commandId, WaitForCommandResponse response) {
        try {
            lock.lock();
            AsyncWaiter waiter = waiters.get(commandId);
            if (waiter == null) {
                waiters.put(commandId, new AsyncWaiter(commandId, response));
            } else {
                if (waiter.getResponse() != null) {
                    // this means that a duplicate Kafka event came through, but
                    // they're idempotent, so we just take the first response.

                    // Basically, just ignore this.
                    log.warn("Just ignoring the second coming of this command id.");
                } else {
                    waiter.setResponse(response);
                    waiter.onMatched();
                    waiters.remove(commandId);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void registerWorkflowEventHappened(WorkflowEventModel event) {
        throw new NotImplementedException();
    }

    public void registerObserverWaitingForWorkflowEvent(WorkflowEventId id, StreamObserver<WorkflowEvent> observer) {
        throw new NotImplementedException();
    }

    public void cleanupOldWaiters() {
        try {
            lock.lock();
            Iterator<Map.Entry<String, AsyncWaiter>> iter = waiters.entrySet().iterator();
            long now = System.currentTimeMillis();
            while (iter.hasNext()) {
                Map.Entry<String, AsyncWaiter> pair = iter.next();
                long age = now - pair.getValue().getArrivalTime().getTime();
                if (age < MAX_WAITER_AGE) {
                    break;
                }
                AsyncWaiter waiter = pair.getValue();
                if (waiter.getObserver() != null) {
                    waiter.getObserver()
                            .onError(new RuntimeException(
                                    "Request not processed on this worker, likely due to" + " rebalance"));
                }
                iter.remove();
            }
        } finally {
            lock.unlock();
        }
    }
}
