package io.littlehorse.sdk.common.auth;

public class AuthorizationServerException extends RuntimeException {

    public AuthorizationServerException(String message) {
        super(message);
    }

    public AuthorizationServerException(Throwable cause) {
        super(cause);
    }
}
