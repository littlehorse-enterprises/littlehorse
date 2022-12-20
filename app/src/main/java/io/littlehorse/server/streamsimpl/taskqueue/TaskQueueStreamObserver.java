package io.littlehorse.server.streamsimpl.taskqueue;

import com.google.rpc.context.AttributeContext.Request;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.PollTaskPb;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.util.LHUtil;

public class TaskQueueStreamObserver implements StreamObserver<PollTaskPb> {

    private StreamObserver<PollTaskReplyPb> responseObserver;
    private TaskQueueManager taskQueueManager;
    private String clientId;
    private String taskDefName;

    public TaskQueueStreamObserver(
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
        LHUtil.log("onError", clientId);
        t.printStackTrace();
    }

    @Override
    public void onNext(PollTaskPb req) {
        System.out.println("hello from onNext(PollTaskPb)");
        if (clientId == null) {
            System.out.println("Setting clientId");
            System.out.println(this);
            clientId = req.getClientId();
        } else if (!clientId.equals(req.getClientId())) {
            // should return a better response than just borking it
            LHUtil.log("Old:", clientId, "requestId: ", req.getClientId(), "yikes");
        }

        if (taskDefName == null) {
            System.out.println("Setting taskdefname");
            System.out.println(this);
            taskDefName = req.getTaskDefName();
        } else if (!taskDefName.equals(req.getTaskDefName())) {
            // should return a better response than just borking it
            System.out.println("weird");
            LHUtil.log("new tdn: ", req.getTaskDefName(), "Old tdn: ", taskDefName);
        }

        taskDefName = req.getTaskDefName();
        clientId = req.getClientId();

        LHUtil.log("onNext, enqueuing client with ID", clientId);
        taskQueueManager.onPollRequest(this);
    }

    @Override
    public void onCompleted() {
        LHUtil.log("OnCompleted for", clientId);
        taskQueueManager.onRequestDisconnected(this);
    }
}
