package io.littlehorse.jlib.common.auth;

public class AuthorizationServerException extends RuntimeException {

    public AuthorizationServerException(String message) {
        super(message);
    }

    public AuthorizationServerException(Throwable cause) {
        super(cause);
    }
}
