package io.littlehorse.server.streams.util;

import io.littlehorse.common.proto.WaitForCommandResponse;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommandWaiter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommandWaiter.class);
    private String commandId;

    private WaitForCommandResponse response;
    private Throwable caughtException;
    private Lock lock;
    private Date arrivalTime;
    private CompletableFuture<WaitForCommandResponse> completableFuture;
    private final int commandPartition;

    private AtomicBoolean alreadyCompleted;

    public CommandWaiter(String commandId, int commandPartition) {
        this.lock = new ReentrantLock();
        this.alreadyCompleted = new AtomicBoolean(false);
        this.commandId = commandId;
        this.arrivalTime = new Date();
        this.commandPartition = commandPartition;
    }

    public boolean setObserverAndMaybeComplete(CompletableFuture<WaitForCommandResponse> completableFuture) {
        try {
            lock.lock();
            if (alreadyCompleted.get()) return false;
            this.completableFuture = completableFuture;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    public boolean setResponseAndMaybeComplete(WaitForCommandResponse response) {
        try {
            lock.lock();
            if (alreadyCompleted.get()) return false;
            this.response = response;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    public boolean setExceptionAndMaybeComplete(Throwable caughtException) {
        try {
            lock.lock();
            if (alreadyCompleted.get()) return false;
            this.caughtException = caughtException;
            return this.maybeMatch();
        } finally {
            lock.unlock();
        }
    }

    private boolean maybeMatch() {
        if (completableFuture == null) return false;
        if (caughtException == null && response == null) return false;

        if (caughtException != null) {
            log.debug("Waiter for command {} is aborting client request due to command process failure", commandId);
            completableFuture.completeExceptionally(caughtException);
        } else {
            completableFuture.complete(response);
            log.debug("Sent response for command {}", commandId);
        }
        alreadyCompleted.set(true);
        return true;
    }

    public void handleMigration() {
        if (completableFuture != null) {
            completableFuture.complete(WaitForCommandResponse.newBuilder()
                    .setPartitionMigratedResponse(WaitForCommandResponse.PartitionMigratedResponse.newBuilder()
                            .build())
                    .build());
        }
    }

    public Date getArrivalTime() {
        return this.arrivalTime;
    }

    public CompletableFuture<WaitForCommandResponse> getCompletableFuture() {
        return this.completableFuture;
    }

    public int getCommandPartition() {
        return this.commandPartition;
    }
}
