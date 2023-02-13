package io.littlehorse.server.streamsimpl.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.jlib.common.proto.WaitForCommandReplyPb;
import java.util.Date;

public class AsyncWaiter {

    public String commandId;
    public StreamObserver<WaitForCommandReplyPb> observer;
    public WaitForCommandReplyPb response;
    public Date arrivalTime;

    public AsyncWaiter() {
        this.arrivalTime = new Date();
    }

    public AsyncWaiter(
        String commandId,
        StreamObserver<WaitForCommandReplyPb> observer
    ) {
        this();
        this.commandId = commandId;
        this.observer = observer;
    }

    public AsyncWaiter(String commandId, WaitForCommandReplyPb response) {
        this();
        this.commandId = commandId;
        this.response = response;
    }

    public void onMatched() {
        if (observer == null) {
            throw new RuntimeException("Invalid call: observer null");
        }
        if (response == null) {
            throw new RuntimeException("Invalid call: response null");
        }
        observer.onNext(response);
        observer.onCompleted();
    }
}
