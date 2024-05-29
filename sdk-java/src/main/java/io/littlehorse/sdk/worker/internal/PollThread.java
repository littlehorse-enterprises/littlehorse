package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollThread extends Thread implements Closeable, StreamObserver<PollTaskResponse> {

    private StreamObserver<PollTaskRequest> pollClient;
    private final String taskWorkerId;
    private final TaskDefId taskDefId;
    private final String taskWorkerVersion;
    private final Semaphore semaphore = new Semaphore(1);

    public final LittleHorseGrpc.LittleHorseStub stub;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final Method taskMethod;
    private final ScheduledTaskExecutor taskExecutor;

    private boolean stillRunning = true;

    public PollThread(
            String threadName,
            LittleHorseGrpc.LittleHorseStub stub,
            TaskDefId taskDefId,
            String taskWorkerId,
            String taskWorkerVersion,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod,
            ScheduledTaskExecutor taskExecutor) {
        super(threadName);
        this.stub = stub;
        this.taskDefId = taskDefId;
        this.taskWorkerId = taskWorkerId;
        this.taskWorkerVersion = taskWorkerVersion;
        this.mappings = mappings;
        this.executable = executable;
        this.taskMethod = taskMethod;
        this.taskMethod.setAccessible(true);
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        this.pollClient = stub.pollTask(this);
        try {
            while (stillRunning) {
                semaphore.acquire();
                pollClient.onNext(PollTaskRequest.newBuilder()
                        .setClientId(taskWorkerId)
                        .setTaskDefId(taskDefId)
                        .setTaskWorkerVersion(taskWorkerVersion)
                        .build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onNext(PollTaskResponse value) {
        if (value.hasResult()) {
            taskExecutor.doTask(value.getResult(), stub, mappings, executable, taskMethod);
        } else {
            log.info("Didn't successfully claim a task");
        }
        semaphore.release();
    }

    @Override
    public void onError(Throwable t) {
        log.error("Unexpected error from server", t);
        this.stillRunning = false;
    }

    @Override
    public void onCompleted() {
        log.error("Unexpected call to onCompleted() in the Server Connection.");
        this.stillRunning = false;
    }

    @Override
    public void close() {
        this.stillRunning = false;
    }

    public boolean isRunning() {
        return this.stillRunning;
    }
}
