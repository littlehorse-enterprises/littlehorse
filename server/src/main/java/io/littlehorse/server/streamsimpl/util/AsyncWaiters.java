package io.littlehorse.server.streamsimpl.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncWaiters {

    public Lock lock;
    public LinkedHashMap<String, AsyncWaiter> waiters;

    private static final long MAX_WAITER_AGE = 1000 * 60;

    public AsyncWaiters() {
        lock = new ReentrantLock();
        waiters = new LinkedHashMap<>();
    }

    // TODO: rename this to register()
    public void put(
        String commandId,
        StreamObserver<WaitForCommandReplyPb> observer
    ) {
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

    // TODO: Rename this to register()
    public void put(String commandId, WaitForCommandReplyPb response) {
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

    public void cleanupOldWaiters() {
        try {
            lock.lock();
            Iterator<Map.Entry<String, AsyncWaiter>> iter = waiters
                .entrySet()
                .iterator();
            long now = System.currentTimeMillis();
            while (iter.hasNext()) {
                Map.Entry<String, AsyncWaiter> pair = iter.next();
                long age = now - pair.getValue().getArrivalTime().getTime();
                if (age < MAX_WAITER_AGE) {
                    break;
                }
                log.debug("Removing from the iter");
                AsyncWaiter waiter = pair.getValue();
                if (waiter.getObserver() != null) {
                    waiter
                        .getObserver()
                        .onError(
                            new RuntimeException(
                                "Request not processed on this worker, likely due to rebalance"
                            )
                        );
                }
                iter.remove();
            }
        } finally {
            lock.unlock();
        }
    }
}
