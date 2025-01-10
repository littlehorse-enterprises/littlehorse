package io.littlehorse.test.internal.step;

import io.littlehorse.test.CapturedResult;

class CapturedResultImpl<T> implements CapturedResult<T> {

    private T value;
    private final Class<T> target;

    public CapturedResultImpl(Class<T> target) {
        this.target = target;
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T result) {
        if (value != null) {
            throw new IllegalStateException("Already has value");
        }
        value = result;
    }

    @Override
    public Class<T> type() {
        return target;
    }
}
