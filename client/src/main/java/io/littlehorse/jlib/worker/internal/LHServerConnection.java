package io.littlehorse.jlib.worker.internal;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.littlehorse.jlib.common.proto.HostInfoPb;
import io.littlehorse.jlib.common.proto.LHPublicApiGrpc;
import io.littlehorse.jlib.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.jlib.common.proto.PollTaskPb;
import io.littlehorse.jlib.common.proto.PollTaskReplyPb;
import java.io.Closeable;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LHServerConnection
    implements Closeable, StreamObserver<PollTaskReplyPb> {

    private Logger log = LoggerFactory.getLogger(LHServerConnection.class);

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

        Channel channel = manager.config.getChannel(host.getHost(), host.getPort());

        this.stub = LHPublicApiGrpc.newStub(channel);
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
            log.info(
                "Received task schedule request for wfRun {}",
                taskToDo.getResult().getWfRunId()
            );
            manager.submitTaskForExecution(taskToDo.getResult(), this.stub);
            log.info(
                "Scheduled task on threadpool for wfRun {}",
                taskToDo.getResult().getWfRunId()
            );
        } else {
            log.error(
                "hmmm: {} {}",
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
