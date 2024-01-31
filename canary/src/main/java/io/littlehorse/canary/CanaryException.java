package io.littlehorse.canary;

public class CanaryException extends RuntimeException {
    public CanaryException() {}

    public CanaryException(String message) {
        super(message);
    }

    public CanaryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanaryException(Throwable cause) {
        super(cause);
    }
}
