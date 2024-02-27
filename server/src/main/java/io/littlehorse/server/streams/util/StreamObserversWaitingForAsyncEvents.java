package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForActionResponse;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamObserversWaitingForAsyncEvents {

    public Lock lock;
    public LinkedHashMap<String, ObserverInWaiting> waiters;

    private static final long MAX_WAITER_AGE = 1000 * 60;

    public StreamObserversWaitingForAsyncEvents() {
        lock = new ReentrantLock();
        waiters = new LinkedHashMap<>();
    }

    public void putObserverWaitingForCommand(String commandId, StreamObserver<WaitForActionResponse> observer) {
        try {
            lock.lock();
            ObserverInWaiting waiter = waiters.get(commandId);
            if (waiter == null) {
                waiters.put(commandId, new ObserverInWaiting(commandId, observer));
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
            ObserverInWaiting waiter = waiters.get(commandId);
            if (waiter == null) {
                waiters.put(commandId, new ObserverInWaiting(commandId, exception));
            } else {
                waiter.setCaughtException(exception);
                waiter.onMatched();
                waiters.remove(commandId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void notifyThatCommandWasProcessed(String commandId, WaitForActionResponse response) {
        try {
            lock.lock();
            ObserverInWaiting waiter = waiters.get(commandId);
            if (waiter == null) {
                waiters.put(commandId, new ObserverInWaiting(commandId, response));
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

    public void cleanupOldWaiters() {
        try {
            lock.lock();
            Iterator<Map.Entry<String, ObserverInWaiting>> iter =
                    waiters.entrySet().iterator();
            long now = System.currentTimeMillis();
            while (iter.hasNext()) {
                Map.Entry<String, ObserverInWaiting> pair = iter.next();
                long age = now - pair.getValue().getArrivalTime().getTime();
                if (age < MAX_WAITER_AGE) {
                    break;
                }
                ObserverInWaiting waiter = pair.getValue();
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
