package io.littlehorse.test;

public interface CapturedResult<T> {

    T get();

    void set(T result);

    Class<T> type();
}
