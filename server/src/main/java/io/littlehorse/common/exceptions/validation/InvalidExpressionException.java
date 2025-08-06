package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

/**
 * Exception thrown when an expression in a workflow specification is invalid.
 * Used to indicate errors in expression parsing or validation.
 */
public class InvalidExpressionException extends LHValidationException {

    public InvalidExpressionException(String message) {
        super(message);
    }
}
