package io.littlehorse.utils;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletedListenableFuture<T> implements ListenableFuture<T> {

    private final T value;

    public CompletedListenableFuture(T value) {
        this.value = value;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        executor.execute(listener);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new IllegalStateException("Cannot cancel a completed future");
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return value;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return value;
    }
}
