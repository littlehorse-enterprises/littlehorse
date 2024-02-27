package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForPedroResponse;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class AsyncWaiter {

    private String commandId;
    private StreamObserver<WaitForPedroResponse> observer;
    private WaitForPedroResponse response;
    private Date arrivalTime;
    private Exception caughtException;

    public AsyncWaiter() {
        this.arrivalTime = new Date();
    }

    public AsyncWaiter(String commandId, StreamObserver<WaitForPedroResponse> observer) {
        this();
        this.commandId = commandId;
        this.observer = observer;
    }

    public AsyncWaiter(String commandId, Exception caughtException) {
        this();
        this.commandId = commandId;
        this.caughtException = caughtException;
    }

    public AsyncWaiter(String commandId, WaitForPedroResponse response) {
        this();
        this.commandId = commandId;
        this.response = response;
    }

    public void onMatched() {
        if (observer == null) {
            throw new RuntimeException("Invalid call: observer null");
        }

        if (caughtException != null) {
            log.info("Waiter is aborting client request due to command process failure");
            observer.onError(caughtException);
        } else if (response != null) {
            observer.onNext(response);
            observer.onCompleted();
        } else {
            // TODO: Throw IllegalStateException?
            log.warn("Impossible: neither response nor exception set.");
        }
    }
}
