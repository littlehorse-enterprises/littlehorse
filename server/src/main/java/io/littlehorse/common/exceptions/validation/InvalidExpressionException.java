package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

public class InvalidExpressionException extends LHValidationException {

    public InvalidExpressionException(String message) {
        super(message);
    }
}
