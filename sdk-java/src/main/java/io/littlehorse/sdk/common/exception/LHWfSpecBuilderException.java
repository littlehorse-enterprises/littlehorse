package io.littlehorse.sdk.common.exception;

/**
 * Thrown when a LittleHorse WfSpec is built incorrectly via the SDK DSL.
 * Indicates a programming error in the workflow definition code.
 */
public class LHWfSpecBuilderException extends RuntimeException {

    public LHWfSpecBuilderException(String message) {
        super(message);
    }

    public LHWfSpecBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
