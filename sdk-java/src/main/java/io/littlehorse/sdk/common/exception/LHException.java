package io.littlehorse.sdk.common.exception;

/**
 * Thrown to indicate that a Task method reached a client specific error.
 */
public class LHException extends Exception {

    public LHException(String message, Throwable cause) {
        super(message, cause);
    }
}
