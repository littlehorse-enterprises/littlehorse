package io.littlehorse.common.model.getable.global.structdef;

import io.littlehorse.common.exceptions.validation.TypeValidationException;

public class StructValidationException extends TypeValidationException {

    public StructValidationException(String msg) {
        super(msg);
    }

    public StructValidationException(Exception exn, String msg) {
        super(exn, msg);
    }
}
