package io.littlehorse.sdk.common.exception;

import lombok.Getter;

/**
 * Thrown to indicate that a Task method reached a client specific error.
 */
@Getter
public class LHTaskException extends Exception {

    private final String name;

    public LHTaskException(String name, String message) {
        super(message);
        this.name = name;
    }

    public LHTaskException(String name, String message, Throwable cause) {
        super(message, cause);
        this.name = name;
    }
}
