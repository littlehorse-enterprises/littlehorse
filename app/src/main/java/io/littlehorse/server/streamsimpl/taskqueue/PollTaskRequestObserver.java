package io.littlehorse.server.streamsimpl.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.jlib.common.proto.PollTaskPb;
import io.littlehorse.jlib.common.proto.PollTaskReplyPb;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollTaskRequestObserver implements StreamObserver<PollTaskPb> {

    private StreamObserver<PollTaskReplyPb> responseObserver;
    private TaskQueueManager taskQueueManager;
    private String clientId;
    private String taskDefName;
    private String taskWorkerVersion;

    public PollTaskRequestObserver(
        StreamObserver<PollTaskReplyPb> responseObserver,
        TaskQueueManager manager
    ) {
        this.responseObserver = responseObserver;
        this.taskQueueManager = manager;
        this.clientId = null;
    }

    public String getTaskWorkerVersion() {
        return taskWorkerVersion;
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
            "Instance {}: Client {} disconnected from task queue {}",
            taskQueueManager.backend.getInstanceId(),
            clientId,
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
                "TaskDefName not null: {} but doesnt match {}",
                taskDefName,
                req.getTaskDefName()
            );
        }

        taskDefName = req.getTaskDefName();
        clientId = req.getClientId();
        taskWorkerVersion = req.getTaskWorkerVersion();

        taskQueueManager.onPollRequest(this);
    }

    @Override
    public void onCompleted() {
        taskQueueManager.onRequestDisconnected(this);
    }
}
