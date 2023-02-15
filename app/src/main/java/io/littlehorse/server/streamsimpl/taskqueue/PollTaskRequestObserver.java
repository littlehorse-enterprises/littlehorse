package io.littlehorse.server.streamsimpl.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.jlib.common.proto.PollTaskPb;
import io.littlehorse.jlib.common.proto.PollTaskReplyPb;
import org.apache.log4j.Logger;

public class PollTaskRequestObserver implements StreamObserver<PollTaskPb> {

    private static final Logger log = Logger.getLogger(PollTaskRequestObserver.class);

    private StreamObserver<PollTaskReplyPb> responseObserver;
    private TaskQueueManager taskQueueManager;
    private String clientId;
    private String taskDefName;

    public PollTaskRequestObserver(
        StreamObserver<PollTaskReplyPb> responseObserver,
        TaskQueueManager manager
    ) {
        this.responseObserver = responseObserver;
        this.taskQueueManager = manager;
        this.clientId = null;
    }

    public String getTaskDefName() {
        return taskDefName;
    }

    public String getClientId() {
        return clientId;
    }

    public StreamObserver<PollTaskReplyPb> getResponseObserver() {
        return responseObserver;
    }

    @Override
    public void onError(Throwable t) {
        log.info(
            "Instance " +
            taskQueueManager.backend.getInstanceId() +
            ": Client " +
            clientId +
            " disconnected from task queue " +
            taskDefName
        );
        taskQueueManager.onRequestDisconnected(this);
    }

    @Override
    public void onNext(PollTaskPb req) {
        if (clientId == null) {
            clientId = req.getClientId();
        }

        if (taskDefName == null) {
            taskDefName = req.getTaskDefName();
        } else if (!taskDefName.equals(req.getTaskDefName())) {
            log.error(
                "TaskDefName not null: " +
                taskDefName +
                " but doesnt match " +
                req.getTaskDefName()
            );
        }

        taskDefName = req.getTaskDefName();
        clientId = req.getClientId();

        taskQueueManager.onPollRequest(this);
    }

    @Override
    public void onCompleted() {
        taskQueueManager.onRequestDisconnected(this);
    }
}
