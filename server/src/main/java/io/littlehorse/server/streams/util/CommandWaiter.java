package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForCommandResponse;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandWaiter {

    private String commandId;

    private WaitForCommandResponse response;
    private Throwable caughtException;
    private Lock lock;

    @Getter
    private Date arrivalTime;

    @Getter
    private StreamObserver<WaitForCommandResponse> observer;

    @Getter
    private final int commandPartition;

    private boolean alreadyCompleted;

    public CommandWaiter(String commandId, int commandPartition) {
        this.lock = new ReentrantLock();
        this.alreadyCompleted = false;
        this.commandId = commandId;
        this.arrivalTime = new Date();
        this.commandPartition = commandPartition;
    }

    public boolean setObserverAndMaybeComplete(StreamObserver<WaitForCommandResponse> observer) {
        try {
            lock.lock();
            if (alreadyCompleted) return false;
            this.observer = observer;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    public boolean setResponseAndMaybeComplete(WaitForCommandResponse response) {
        try {
            lock.lock();
            if (alreadyCompleted) return false;
            this.response = response;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    public boolean setExceptionAndMaybeComplete(Throwable caughtException) {
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
        this.alreadyCompleted = true;
        return true;
    }

    public void handleMigration() {
        if (observer != null) {
            observer.onNext(WaitForCommandResponse.newBuilder()
                    .setPartitionMigratedResponse(WaitForCommandResponse.PartitionMigratedResponse.newBuilder()
                            .build())
                    .build());
            observer.onCompleted();
        }
    }
}
