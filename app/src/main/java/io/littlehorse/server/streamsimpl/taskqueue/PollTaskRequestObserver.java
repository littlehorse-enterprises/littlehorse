package io.littlehorse.server.streamsimpl.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.PollTaskPb;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.util.LHUtil;

public class PollTaskRequestObserver implements StreamObserver<PollTaskPb> {

    private StreamObserver<PollTaskReplyPb> responseObserver;
    private TaskQueueManager taskQueueManager;
    private String clientId;
    private String taskDefName;
    private String guid;

    public PollTaskRequestObserver(
        StreamObserver<PollTaskReplyPb> responseObserver,
        TaskQueueManager manager
    ) {
        this.guid = LHUtil.generateGuid();
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
        LHUtil.log("Guid: ", guid);

        if (clientId == null) {
            clientId = req.getClientId();
            LHUtil.log("Just set clientId:", this.clientId);
        } else if (!clientId.equals(req.getClientId())) {
            LHUtil.log(
                "Client Id not null: ",
                clientId,
                "but doesnt match " + req.getClientId()
            );
        }

        if (taskDefName == null) {
            taskDefName = req.getTaskDefName();
            LHUtil.log("Just set taskDefName to " + taskDefName);
        } else if (!taskDefName.equals(req.getTaskDefName())) {
            LHUtil.log(
                "TaskDefName not null: ",
                taskDefName,
                "but doesnt match " + req.getTaskDefName()
            );
        }

        taskDefName = req.getTaskDefName();
        clientId = req.getClientId();

        LHUtil.log("TaskQueue is now enqueueing observer with id: ", clientId);
        taskQueueManager.onPollRequest(this);
    }

    @Override
    public void onCompleted() {
        LHUtil.log("OnCompleted for", clientId);
        taskQueueManager.onRequestDisconnected(this);
    }
}
