package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

public class InvalidWfSpecException extends LHValidationException {

    public InvalidWfSpecException(InvalidThreadSpecException cause) {
        super("PutWfSpecRequest is invalid: " + cause.getMessage());
    }

    public InvalidWfSpecException(String message) {
        super(message);
    }
}
