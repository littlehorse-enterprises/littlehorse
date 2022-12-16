package io.littlehorse.server.streamsbackend.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.PollTaskPb;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.util.LHUtil;

public class TaskQueueStreamObserver implements StreamObserver<PollTaskPb> {

    private StreamObserver<PollTaskReplyPb> responseObserver;
    private GodzillaTaskQueueManager manager;
    private String clientId;
    private String taskDefName;

    public TaskQueueStreamObserver(
        StreamObserver<PollTaskReplyPb> responseObserver,
        GodzillaTaskQueueManager manager
    ) {
        this.responseObserver = responseObserver;
        this.manager = manager;
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
        manager.onRequestDisconnected(this);
        LHUtil.log("onError", clientId);
        t.printStackTrace();
    }

    @Override
    public void onNext(PollTaskPb req) {
        if (clientId == null) clientId = req.getClientId();
        if (!clientId.equals(req.getClientId())) {
            // should return a better response than just borking it
            throw new RuntimeException("Not possible");
        }

        if (taskDefName == null) clientId = req.getTaskDefName();
        if (!taskDefName.equals(req.getTaskDefName())) {
            // should return a better response than just borking it
            throw new RuntimeException("Not possible");
        }

        LHUtil.log("onNext, enqueuing", clientId);
        manager.onPollRequest(this);
    }

    @Override
    public void onCompleted() {
        LHUtil.log("OnCompleted for", clientId);
        manager.onRequestDisconnected(this);
    }
}
