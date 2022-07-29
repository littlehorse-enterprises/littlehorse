package io.littlehorse.common.exceptions;

public class LHConnectionError extends LHException {
    private Exception parent;
    private String message;

    public LHConnectionError(Exception exn, String msg) {
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