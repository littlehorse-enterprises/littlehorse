package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.WaitForActionResponse;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class ObserverInWaiting {

    private String commandId;
    private StreamObserver<WaitForActionResponse> observer;
    private WaitForActionResponse response;
    private Date arrivalTime;
    private Exception caughtException;

    public ObserverInWaiting() {
        this.arrivalTime = new Date();
    }

    public ObserverInWaiting(String commandId, StreamObserver<WaitForActionResponse> observer) {
        this();
        this.commandId = commandId;
        this.observer = observer;
    }

    public ObserverInWaiting(String commandId, Exception caughtException) {
        this();
        this.commandId = commandId;
        this.caughtException = caughtException;
    }

    public ObserverInWaiting(String commandId, WaitForActionResponse response) {
        this();
        this.commandId = commandId;
        this.response = response;
    }

    public void onMatched() {
        if (observer == null) {
            throw new IllegalStateException("Invalid call: observer null");
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
