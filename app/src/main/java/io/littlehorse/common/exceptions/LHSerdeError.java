package io.littlehorse.common.exceptions;

public class LHSerdeError extends LHException {
    public LHSerdeError(Exception exn, String msg) {
        super(exn, msg);
    }
}
