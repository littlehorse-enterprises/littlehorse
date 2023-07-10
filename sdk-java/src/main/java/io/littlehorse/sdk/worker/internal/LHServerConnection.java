package io.littlehorse.sdk.worker.internal;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.HostInfoPb;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.sdk.common.proto.PollTaskPb;
import io.littlehorse.sdk.common.proto.PollTaskReplyPb;
import io.littlehorse.sdk.common.proto.ScheduledTaskPb;
import java.io.Closeable;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHServerConnection
    implements Closeable, StreamObserver<PollTaskReplyPb> {

    private LHServerConnectionManager manager;
    private HostInfoPb host;

    private boolean stillRunning;
    private StreamObserver<PollTaskPb> pollClient;
    private LHPublicApiStub stub;

    public LHServerConnection(LHServerConnectionManager manager, HostInfoPb host)
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
    public void onNext(PollTaskReplyPb taskToDo) {
        if (taskToDo.hasResult()) {
            ScheduledTaskPb scheduledTask = taskToDo.getResult();
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

    public HostInfoPb getHostInfo() {
        return host;
    }

    public boolean isSameAs(HostInfoPb other) {
        return (
            this.host.getHost().equals(other.getHost()) &&
            this.host.getPort() == other.getPort()
        );
    }

    private void askForMoreWork() {
        log.debug("Asking for more work on {}:{}", host.getHost(), host.getPort());
        pollClient.onNext(
            PollTaskPb
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
