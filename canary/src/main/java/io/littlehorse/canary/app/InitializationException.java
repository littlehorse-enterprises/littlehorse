package io.littlehorse.canary.app;

public class InitializationException extends RuntimeException {
    public InitializationException() {}

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializationException(Throwable cause) {
        super(cause);
    }
}
