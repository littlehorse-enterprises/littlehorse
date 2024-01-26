package io.littlehorse.canary.app;

public class BoostrapInitializationException extends Exception {
    public BoostrapInitializationException() {}

    public BoostrapInitializationException(String message) {
        super(message);
    }

    public BoostrapInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoostrapInitializationException(Throwable cause) {
        super(cause);
    }
}
