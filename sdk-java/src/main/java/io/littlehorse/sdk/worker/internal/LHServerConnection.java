package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import java.io.Closeable;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHServerConnection
    implements Closeable, StreamObserver<PollTaskResponse> {

    private LHServerConnectionManager manager;
    private HostInfo host;

    private boolean stillRunning;
    private StreamObserver<PollTaskRequest> pollClient;
    private LHPublicApiStub stub;

    public LHServerConnection(LHServerConnectionManager manager, HostInfo host)
        throws IOException {
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
            String wfRunId = LHLibUtil.getWfRunId(scheduledTask.getSource());
            log.info("Received task schedule request for wfRun {}", wfRunId);

            manager.submitTaskForExecution(scheduledTask, this.stub);

            log.info("Scheduled task on threadpool for wfRun {}", wfRunId);
        } else {
            log.error(
                "Didn't successfully claim task: {} {}",
                taskToDo.getCode().toString(),
                taskToDo.getMessage()
            );
        }

        if (stillRunning) {
            askForMoreWork();
        } else {
            // This may cause issues when there's multiple threads, eg closing an
            // already closed stream observer.
            pollClient.onCompleted();
        }
    }

    public HostInfo getHostInfo() {
        return host;
    }

    public boolean isSameAs(HostInfo other) {
        return (
            this.host.getHost().equals(other.getHost()) &&
            this.host.getPort() == other.getPort()
        );
    }

    private void askForMoreWork() {
        log.debug("Asking for more work on {}:{}", host.getHost(), host.getPort());
        pollClient.onNext(
            PollTaskRequest
                .newBuilder()
                .setClientId(manager.config.getClientId())
                .setTaskDefName(manager.taskDef.getName())
                .setTaskWorkerVersion(manager.config.getTaskWorkerVersion())
                .build()
        );
    }

    public void close() {
        stillRunning = false;

        // Let the LH server know we're done
        pollClient.onCompleted();
    }
}
