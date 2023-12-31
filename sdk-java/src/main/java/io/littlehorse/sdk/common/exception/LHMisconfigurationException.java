package io.littlehorse.sdk.common.exception;

public class LHMisconfigurationException extends RuntimeException {

    public LHMisconfigurationException(String message) {
        super(message);
    }

    public LHMisconfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
