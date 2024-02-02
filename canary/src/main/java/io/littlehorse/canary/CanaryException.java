package io.littlehorse.canary;

public class CanaryException extends RuntimeException {

    public CanaryException(final Throwable cause) {
        super(cause);
    }
}
