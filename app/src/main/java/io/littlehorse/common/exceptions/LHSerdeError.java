package io.littlehorse.common.exceptions;

public class LHSerdeError extends LHException {
    private Exception parent;
    private String message;

    public LHSerdeError(Exception exn, String msg) {
        this.message = msg;
        this.parent = exn;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public Exception parent() {
        return this.parent;
    }
}
