package io.littlehorse.server.streams.util;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncWaiter<T extends Message> {

    private String commandId;

    private T response;
    private Exception caughtException;
    private Lock lock;

    @Getter
    private Date arrivalTime;

    @Getter
    private StreamObserver<T> observer;

    private boolean alreadyCompleted;

    public AsyncWaiter(String commandId) {
        this.lock = new ReentrantLock();
        this.alreadyCompleted = false;
        this.commandId = commandId;
        this.arrivalTime = new Date();
    }

    public boolean setObserverAndMaybeComplete(StreamObserver<T> observer) {
        try {
            lock.lock();
            if (alreadyCompleted) return false;
            this.observer = observer;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    public boolean setResponseAndMaybeComplete(T response) {
        try {
            lock.lock();
            if (alreadyCompleted) return false;
            this.response = response;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    public boolean setExceptionAndMaybeComplete(Exception caughtException) {
        try {
            lock.lock();
            if (alreadyCompleted) return false;
            this.caughtException = caughtException;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    private boolean maybeMatch() {
        if (observer == null) return false;
        if (caughtException == null && response == null) return false;

        if (caughtException != null) {
            log.debug("Waiter for command {} is aborting client request due to command process failure", commandId);
            observer.onError(caughtException);
        } else if (response != null) {
            observer.onNext(response);
            observer.onCompleted();
            log.debug("Sent response for command {}", commandId);
        }
        return true;
    }
}
