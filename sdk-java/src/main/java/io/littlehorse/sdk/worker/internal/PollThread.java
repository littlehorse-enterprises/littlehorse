package io.littlehorse.sdk.worker.internal;

import com.google.common.collect.Iterators;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollThread extends Thread implements Closeable {

    private Iterator<StreamObserver<PollTaskRequest>> activePollClients;
    private final String taskWorkerId;
    private final TaskDefId taskDefId;
    private final String taskWorkerVersion;
    private final Semaphore semaphore = new Semaphore(2);

    public final LittleHorseGrpc.LittleHorseStub stub;
    private final List<VariableMapping> mappings;
    private final Object executable;
    private final Method taskMethod;
    private final ScheduledTaskExecutor taskExecutor;

    private boolean stillRunning = true;
    private final LHConfig config;

    public PollThread(
            String threadName,
            LittleHorseGrpc.LittleHorseStub stub,
            TaskDefId taskDefId,
            String taskWorkerId,
            String taskWorkerVersion,
            List<VariableMapping> mappings,
            Object executable,
            Method taskMethod,
            ScheduledTaskExecutor taskExecutor,
            LHConfig config) {
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
        this.config = config;
    }

    @Override
    public void run() {
        List<StreamObserver<PollTaskRequest>> pollClients = Stream.generate(this::createObserver).limit(2).collect(Collectors.toList());
        this.activePollClients = Iterators.cycle(pollClients);
        try {
            while (stillRunning) {
                semaphore.acquire();
                StreamObserver<PollTaskRequest> pollClient = activePollClients.next();
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

    private StreamObserver<PollTaskRequest> createObserver() {
        LittleHorseGrpc.LittleHorseStub specificStub = config.getAsyncStub();
        return stub.pollTask(new ServerResponseObserver(specificStub));
    }

    @Override
    public void close() {
        this.stillRunning = false;
    }

    public boolean isRunning() {
        return this.stillRunning;
    }

    private final class ServerResponseObserver implements StreamObserver<PollTaskResponse>{
        private final LittleHorseGrpc.LittleHorseStub specificStub;
        private ServerResponseObserver(LittleHorseGrpc.LittleHorseStub specificStub) {
            this.specificStub = specificStub;
        }

        @Override
        public void onNext(PollTaskResponse value) {
            if (value.hasResult()) {
                taskExecutor.doTask(value.getResult(), specificStub, mappings, executable, taskMethod);
            } else {
                log.info("Didn't successfully claim a task");
            }
            semaphore.release();
        }

        @Override
        public void onError(Throwable t) {
            log.error("Unexpected error from server", t);
        }

        @Override
        public void onCompleted() {
            log.error("Unexpected call to onCompleted() in the Server Connection.");
            stillRunning = false;
        }
    }
}
