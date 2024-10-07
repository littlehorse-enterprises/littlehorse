package io.littlehorse.sdk.common.exception;

public class EntityProviderException extends RuntimeException {

    public EntityProviderException(String message) {
        super(message);
    }

    public EntityProviderException(Throwable cause) {
        super(cause);
    }
}
