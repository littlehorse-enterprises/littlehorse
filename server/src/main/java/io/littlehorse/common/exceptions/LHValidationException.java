package io.littlehorse.common.exceptions;

public class LHValidationException extends LHException {

    public LHValidationException(Exception exn, String msg) {
        super(exn, msg);
    }

    public LHValidationException(String msg) {
        super(null, msg);
    }
}
