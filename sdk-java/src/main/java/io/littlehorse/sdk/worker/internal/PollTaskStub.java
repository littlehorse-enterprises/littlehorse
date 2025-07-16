package io.littlehorse.sdk.worker.internal;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PollTaskStub implements AutoCloseable {
    private final ServerResponseObserver responseObserver;
    private final Semaphore semaphore;
    private final ScheduledTaskExecutor taskExecutor;
    private final AtomicBoolean ready = new AtomicBoolean(true);
    private final StreamObserver<PollTaskRequest> observer;
    private final String taskWorkerId;
    private final TaskDefId taskDefId;
    private final String taskWorkerVersion;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final Method taskMethod;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public PollTaskStub(
            LittleHorseGrpc.LittleHorseStub bootstrapStub,
            LittleHorseGrpc.LittleHorseStub specificStub,
            Semaphore semaphore,
            ScheduledTaskExecutor taskExecutor,
            String taskWorkerId,
            TaskDefId taskDefId,
            String taskWorkerVersion,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod) {
        this.responseObserver = new ServerResponseObserver(bootstrapStub);
        this.semaphore = semaphore;
        this.taskExecutor = taskExecutor;
        this.taskWorkerId = taskWorkerId;
        this.taskDefId = taskDefId;
        this.taskWorkerVersion = taskWorkerVersion;
        this.mappings = mappings;
        this.executable = executable;
        this.taskMethod = taskMethod;
        this.observer = specificStub.pollTask(responseObserver);
    }

    public boolean isReady() {
        return ready.get();
    }

    public boolean isClosed() {
        return closed.get();
    }

    public void doNext() throws InterruptedException {
        semaphore.acquire();
        ready.set(false);
        observer.onNext(PollTaskRequest.newBuilder()
                .setClientId(taskWorkerId)
                .setTaskDefId(taskDefId)
                .setTaskWorkerVersion(taskWorkerVersion)
                .build());
    }

    public void acquireNextPermit() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public void close() {
        try {
            observer.onCompleted();
        } catch (IllegalStateException ignored) {
            // Already completed
        }
    }

    private final class ServerResponseObserver implements StreamObserver<PollTaskResponse> {
        private final LittleHorseGrpc.LittleHorseStub bootstrapStub;

        private ServerResponseObserver(LittleHorseGrpc.LittleHorseStub bootstrapStub) {
            this.bootstrapStub = bootstrapStub;
        }

        @Override
        public void onNext(PollTaskResponse value) {
            if (value.hasResult()) {
                taskExecutor.doTask(value.getResult(), bootstrapStub, mappings, executable, taskMethod);
            } else {
                log.info("Didn't successfully claim a task");
            }
            semaphore.release();
            ready.set(true);
        }

        @Override
        public void onError(Throwable t) {
            if (t instanceof StatusRuntimeException
                    && ((StatusRuntimeException) t).getStatus().getCode().equals(Status.CANCELLED.getCode())) {
                log.debug("Connection closed");
            } else {
                log.error("Unexpected error from server", t);
            }
            closed.set(true);
        }

        @Override
        public void onCompleted() {
            log.error("Unexpected call to onCompleted() in the Server Connection.");
            closed.set(true);
        }
    }
}
