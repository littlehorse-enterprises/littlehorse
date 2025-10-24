package io.littlehorse.common.model.getable.global.structdef;

import io.littlehorse.common.exceptions.LHValidationException;

public class StructValidationException extends LHValidationException {

    public StructValidationException(String msg) {
        super(msg);
    }

    public StructValidationException(Exception exn, String msg) {
        super(exn, msg);
    }
}
