package io.littlehorse.common.exceptions;

public class LHBadRequestError extends LHException {

    public LHBadRequestError(Exception exn, String msg) {
        super(exn, msg);
    }

    public LHBadRequestError(String msg) {
        super(null, msg);
    }
}
