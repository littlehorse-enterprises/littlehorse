package io.littlehorse.common;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class TestStreamObserver<T> implements StreamObserver<T> {

    private final List<T> values = new ArrayList<>();
    private Throwable throwable;
    private boolean completed;

    @Override
    public void onNext(T value) {
        this.values.add(value);
    }

    @Override
    public void onError(Throwable t) {
        if (this.throwable != null) {
            throw new IllegalStateException("Already failed");
        }
        this.throwable = t;
    }

    @Override
    public void onCompleted() {
        if (completed) {
            throw new IllegalStateException("Already completed");
        }
        this.completed = true;
    }
}
