package io.littlehorse.test.exception;

public class LHTestInitializationException extends RuntimeException {

    public LHTestInitializationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
