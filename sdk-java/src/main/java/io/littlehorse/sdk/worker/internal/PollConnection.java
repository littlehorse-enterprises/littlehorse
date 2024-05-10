package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages an active connection to poll tasks from the server.
 */
@Slf4j
public class PollConnection implements Closeable, StreamObserver<PollTaskResponse> {

    private final LHTaskExecutor executor;
    private LHHostInfo host;

    @Getter
    private boolean stillRunning;

    private StreamObserver<PollTaskRequest> pollClient;
    private LittleHorseStub stub;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final Method taskMethod;
    private final String taskWorkerId;
    private final TaskDefId taskDefId;
    private final String taskWorkerVersion;

    public PollConnection(
            LHTaskExecutor executor,
            LHHostInfo host,
            LittleHorseStub stub,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod,
            String taskWorkerId,
            TaskDefId taskDefId,
            String taskWorkerVersion) {
        stillRunning = true;
        this.mappings = mappings;
        this.executable = executable;
        this.taskMethod = taskMethod;
        this.taskMethod.setAccessible(true);
        this.executor = executor;
        this.host = host;
        this.stub = stub;
        this.pollClient = this.stub.pollTask(this);
        this.taskWorkerId = taskWorkerId;
        this.taskDefId = taskDefId;
        this.taskWorkerVersion = taskWorkerVersion;

        log.info("New connection to: " + this);
        askForMoreWork();
    }

    @Override
    public void onCompleted() {
        // This shouldn't happen.
        log.error("Unexpected call to onCompleted() in the Server Connection.");
        this.stillRunning = false;
    }

    @Override
    public void onError(Throwable t) {
        log.error("Unexpected error from server", t);
        this.stillRunning = false;
    }

    @Override
    public void onNext(PollTaskResponse taskToDo) {
        if (taskToDo.hasResult()) {
            ScheduledTask scheduledTask = taskToDo.getResult();
            executor.submitTaskForExecution(scheduledTask, stub, mappings, executable, taskMethod);
        }

        if (stillRunning) {
            askForMoreWork();
        } else {
            // This may cause issues when there's multiple threads, eg closing an
            // already closed stream observer.
            pollClient.onCompleted();
        }
    }

    private void askForMoreWork() {
        log.debug("Asking for more work on {}:{}", host.getHost(), host.getPort());
        executor.acquire();
        pollClient.onNext(PollTaskRequest.newBuilder()
                .setClientId(taskWorkerId)
                .setTaskDefId(taskDefId)
                .setTaskWorkerVersion(taskWorkerVersion)
                .build());
    }

    @Override
    public String toString() {
        return "Connection to " + host.getHost() + ":" + host.getPort();
    }

    @Override
    public void close() {
        log.info("Stopping connection to " + this);
        stillRunning = false;

        // Let the LH server know we're done
        pollClient.onCompleted();
    }
}
