package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

/**
 * Exception thrown when an interrupt definition in a workflow specification is invalid.
 * Used to signal issues with interrupt configuration or validation.
 */
public class InvalidInterruptDefException extends LHValidationException {

    public InvalidInterruptDefException(String message) {
        super(message);
    }
}
