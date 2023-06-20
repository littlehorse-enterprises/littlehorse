package io.littlehorse.jlib.common.exception;

import io.littlehorse.jlib.common.proto.LHResponseCodePb;

public class LHApiError extends Exception {

    private LHResponseCodePb code;
    private String message;
    private Throwable parent;

    public LHApiError(String message, LHResponseCodePb code) {
        this.code = code;
        this.message = message;
    }

    public LHApiError(Throwable cause, String message) {
        this.message = message;
        this.code = LHResponseCodePb.CONNECTION_ERROR;
        this.parent = cause;
    }

    public LHApiError(Throwable cause, String message, LHResponseCodePb code) {
        this.message = message;
        this.code = code;
        this.parent = cause;
    }

    public LHResponseCodePb getCode() {
        return code;
    }

    public Throwable getCause() {
        return parent;
    }

    public String getMessage() {
        String out = "Failed contacting LH API: " + code + ": " + message;
        if (parent != null) {
            out += ": " + parent.getMessage();
        }
        return out;
    }
}
