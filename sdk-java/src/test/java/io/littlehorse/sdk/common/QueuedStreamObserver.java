package io.littlehorse.sdk.common;

import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Setter;

public class QueuedStreamObserver<REQ, RES> {
    private final RequestObserver requestObserver;
    private final StreamObserver<RES> responseObserver;
    private final Queue<NextAction> pendingResponses = new LinkedBlockingQueue<>();

    public QueuedStreamObserver(StreamObserver<RES> responseObserver) {
        this.requestObserver = new RequestObserver();
        this.responseObserver = responseObserver;
    }

    public void record(RES response) {
        pendingResponses.offer(new ResponseAction(response));
    }

    public void record(Throwable failure) {
        pendingResponses.add(new FailureAction(failure));
    }

    public StreamObserver<REQ> getRequestObserver() {
        return requestObserver;
    }

    private class RequestObserver implements StreamObserver<REQ> {

        @Override
        public void onNext(REQ value) {
            pollNextAction().ifPresent(NextAction::doNext);
        }

        @Override
        public void onError(Throwable t) {
            pollNextAction().ifPresent(NextAction::doNext);
        }

        @Override
        public void onCompleted() {}
    }

    public static class DelegatedStreamObserver<REQ> implements StreamObserver<REQ> {

        @Setter
        private StreamObserver<REQ> observer;

        public final AtomicBoolean completed = new AtomicBoolean(false);

        @Override
        public void onNext(REQ value) {
            observer.onNext(value);
        }

        @Override
        public void onError(Throwable t) {
            observer.onError(t);
        }

        @Override
        public void onCompleted() {
            observer.onCompleted();
            completed.set(true);
        }

        public boolean isCompleted() {
            return completed.get();
        }
    }

    private Optional<NextAction> pollNextAction() {
        NextAction next = pendingResponses.poll();
        if (next == null) {
            responseObserver.onCompleted();
        }
        return Optional.ofNullable(next);
    }

    private interface NextAction {

        void doNext();
    }

    private class ResponseAction implements NextAction {
        private final RES response;

        ResponseAction(RES response) {
            this.response = response;
        }

        @Override
        public void doNext() {
            responseObserver.onNext(response);
        }
    }

    private class FailureAction implements NextAction {
        private final Throwable failure;

        FailureAction(Throwable failure) {
            this.failure = failure;
        }

        @Override
        public void doNext() {
            responseObserver.onError(failure);
        }
    }
}
