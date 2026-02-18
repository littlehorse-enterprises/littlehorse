package io.littlehorse.common.model.getable.global.structdef;

import io.littlehorse.common.exceptions.LHValidationException;

public class StructDefValidationException extends LHValidationException {

    public StructDefValidationException(String msg) {
        super(msg);
    }

    public StructDefValidationException(Exception exn, String msg) {
        super(exn, msg);
    }
}
