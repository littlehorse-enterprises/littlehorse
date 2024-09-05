package io.littlehorse.server.streams.util;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SafeStreamObserver<T> implements StreamObserver<T> {
    private final StreamObserver<T> wrapped;

    public SafeStreamObserver(final StreamObserver<T> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void onNext(T value) {
        doSafe(() -> wrapped.onNext(value));
    }

    @Override
    public void onError(Throwable t) {
        doSafe(() -> wrapped.onError(t));
    }

    @Override
    public void onCompleted() {
        doSafe(wrapped::onCompleted);
    }

    private void doSafe(Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalStateException e) {
            log.warn("Ignored exception");
        }
    }
}
