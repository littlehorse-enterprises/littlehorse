package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

/**
 * Exception thrown when a workflow specification is invalid.
 * Used to indicate errors in workflow definition or validation.
 */
public class InvalidWfSpecException extends LHValidationException {

    public InvalidWfSpecException(InvalidThreadSpecException cause) {
        super("PutWfSpecRequest is invalid: " + cause.getMessage());
    }

    public InvalidWfSpecException(String message) {
        super(message);
    }
}
