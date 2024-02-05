package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import java.io.Closeable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHServerConnection implements Closeable, StreamObserver<PollTaskResponse> {

    private LHServerConnectionManager manager;
    private LHHostInfo host;

    private boolean stillRunning;
    private StreamObserver<PollTaskRequest> pollClient;
    private LittleHorseStub stub;

    public LHServerConnection(LHServerConnectionManager manager, LHHostInfo host) {
        stillRunning = true;
        this.manager = manager;
        this.host = host;

        this.stub = manager.config.getAsyncStub(host.getHost(), host.getPort());

        this.pollClient = this.stub.pollTask(this);

        askForMoreWork();
    }

    @Override
    public void onCompleted() {
        // This shouldn't happen.
        log.error("Unexpected call to onCompleted() in the Server Connection.");
        this.stillRunning = false;
        manager.onConnectionClosed(this);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Unexpected error from server", t);
        this.stillRunning = false;
        manager.onConnectionClosed(this);
    }

    @Override
    public void onNext(PollTaskResponse taskToDo) {
        if (taskToDo.hasResult()) {
            ScheduledTask scheduledTask = taskToDo.getResult();
            String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource()).getId();
            log.debug("Received task schedule request for wfRun {}", wfRunId);

            manager.submitTaskForExecution(scheduledTask, this.stub);

            log.debug("Scheduled task on threadpool for wfRun {}", wfRunId);
        } else {
            log.error("Didn't successfully claim task, likely due to server restart.");
        }

        if (stillRunning) {
            askForMoreWork();
        } else {
            // This may cause issues when there's multiple threads, eg closing an
            // already closed stream observer.
            pollClient.onCompleted();
        }
    }

    public LHHostInfo getHostInfo() {
        return host;
    }

    public boolean isSameAs(LHHostInfo other) {
        return (this.host.getHost().equals(other.getHost()) && this.host.getPort() == other.getPort());
    }

    private void askForMoreWork() {
        log.debug("Asking for more work on {}:{}", host.getHost(), host.getPort());
        pollClient.onNext(PollTaskRequest.newBuilder()
                .setClientId(manager.config.getTaskWorkerId())
                .setTaskDefId(manager.taskDef.getId())
                .setTaskWorkerVersion(manager.config.getTaskWorkerVersion())
                .build());
    }

    public void close() {
        stillRunning = false;

        // Let the LH server know we're done
        pollClient.onCompleted();
    }
}
