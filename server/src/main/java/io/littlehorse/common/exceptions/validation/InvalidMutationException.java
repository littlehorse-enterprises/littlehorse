package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

public class InvalidMutationException extends LHValidationException {

    public InvalidMutationException(String message) {
        super(message);
    }
}
