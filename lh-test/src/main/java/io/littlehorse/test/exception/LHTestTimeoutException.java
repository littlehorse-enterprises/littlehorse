package io.littlehorse.test.exception;

public class LHTestTimeoutException extends RuntimeException {

    public LHTestTimeoutException(String message) {
        super(message);
    }

    public LHTestTimeoutException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
