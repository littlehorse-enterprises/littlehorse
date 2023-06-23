package io.littlehorse.server.auth;

public class UnexpectedAuthorizationServerException extends RuntimeException {

    public UnexpectedAuthorizationServerException(String message) {
        super(message);
    }

    public UnexpectedAuthorizationServerException(Throwable cause) {
        super(cause);
    }
}
