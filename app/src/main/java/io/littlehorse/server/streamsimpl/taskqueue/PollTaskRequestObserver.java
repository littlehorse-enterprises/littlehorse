package io.littlehorse.server.streamsimpl.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.PollTaskPb;
import io.littlehorse.jlib.common.proto.PollTaskReplyPb;

public class PollTaskRequestObserver implements StreamObserver<PollTaskPb> {

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
        taskQueueManager.onRequestDisconnected(this);
        LHUtil.log("Client", clientId, ": disconnected.");
    }

    @Override
    public void onNext(PollTaskPb req) {
        if (clientId == null) {
            clientId = req.getClientId();
        }

        if (taskDefName == null) {
            taskDefName = req.getTaskDefName();
        } else if (!taskDefName.equals(req.getTaskDefName())) {
            LHUtil.log(
                "TaskDefName not null: ",
                taskDefName,
                "but doesnt match " + req.getTaskDefName()
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
