package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

/**
 * Exception thrown when a mutation in a workflow specification is invalid.
 * Used to indicate errors in mutation definition or validation.
 */
public class InvalidMutationException extends LHValidationException {

    public InvalidMutationException(String message) {
        super(message);
    }
}
