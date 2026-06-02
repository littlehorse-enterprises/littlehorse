package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

/**
 * Base exception for type/compatibility validation errors in model layer.
 * Transport-layer code should map this to LHApiException as needed.
 */
public class TypeValidationException extends LHValidationException {
    public TypeValidationException(String message) {
        super(message);
    }

    public TypeValidationException(Exception exn, String message) {
        super(exn, message);
    }
}
