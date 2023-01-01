package io.littlehorse.server.streamsimpl.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHUtil;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncWaiters {

    public Lock lock;
    public LinkedHashMap<String, AsyncWaiter> waiters;

    private static final int MAX_WAITER_SIZE = 1000;
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
                if (waiter.observer != null) {
                    // this means the request has come in again...
                    waiter.observer = observer;
                    LHUtil.log(
                        "Warn: got a retry request before the event was processed"
                    );
                } else {
                    waiter.observer = observer;
                    waiter.onMatched();
                    waiters.remove(commandId);
                }
            }

            if (waiters.size() > MAX_WAITER_SIZE) {
                cleanupOldWaiters();
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
                if (waiter.response != null) {
                    // this means that a duplicate Kafka event came through, but
                    // they're idempotent, so we just take the first response.

                    // Basically, just ignore this.
                    LHUtil.log(
                        "Warn: just ignoring the second coming of this command id."
                    );
                } else {
                    waiter.response = response;
                    waiter.onMatched();
                    waiters.remove(commandId);
                }
            }

            if (waiters.size() > MAX_WAITER_SIZE) {
                cleanupOldWaiters();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @precondition the caller owns the lock on this.lock.
     */
    private void cleanupOldWaiters() {
        Iterator<Map.Entry<String, AsyncWaiter>> iter = waiters.entrySet().iterator();
        long now = System.currentTimeMillis();
        while (iter.hasNext()) {
            Map.Entry<String, AsyncWaiter> pair = iter.next();
            long age = now - pair.getValue().arrivalTime.getTime();
            if (age < MAX_WAITER_AGE) {
                break;
            }
            LHUtil.log("removing from the iter");
            iter.remove();
        }
    }
}
