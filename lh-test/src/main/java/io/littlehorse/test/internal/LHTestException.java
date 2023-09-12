package io.littlehorse.test.internal;

public class LHTestException extends RuntimeException {

    public LHTestException() {}

    public LHTestException(String message) {
        super(message);
    }

    public LHTestException(Throwable cause) {
        super(cause);
    }

    public LHTestException(String message, Throwable cause) {
        super(message, cause);
    }
}
