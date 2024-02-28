package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowEventWaiter {

    private WaitForCommandResponse response;
    private Exception caughtException;
    private WorkflowEventIdModel eventId;
    private Lock lock;

    @Getter
    private Date arrivalTime;

    @Getter
    private StreamObserver<WorkflowEvent> observer;

    private boolean alreadyCompleted;

    public WorkflowEventWaiter(WorkflowEventIdModel eventId, StreamObserver<WorkflowEvent> observer) {
        this.lock = new ReentrantLock();
        this.alreadyCompleted = false;
        this.arrivalTime = new Date();
        this.eventId = eventId;
        this.observer = observer;
    }

    public boolean completeWithEvent(WorkflowEventModel event) {
        try {
            lock.lock();
            if (alreadyCompleted) return false;
            alreadyCompleted = true;
        } finally {
            lock.unlock();
        }
        observer.onNext(event.toProto().build());
        observer.onCompleted();
        return true;
    }

    // private boolean maybeMatch() {
    //     if (observer == null) return false;
    //     if (caughtException == null && response == null) return false;

    //     if (caughtException != null) {
    //         log.debug(
    //                 "Waiter for command {} is aborting client request due to command process failure",
    //                 eventId.toString());
    //         observer.onError(caughtException);
    //     } else if (response != null) {
    //         observer.onNext(response);
    //         observer.onCompleted();
    //     }
    //     return true;
    // }
}
