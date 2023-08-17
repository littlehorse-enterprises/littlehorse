package io.littlehorse.sdk.common.exception;

import io.littlehorse.sdk.common.proto.LHResponseCode;

public class LHApiError extends Exception {

    private LHResponseCode code;
    private String message;
    private Throwable parent;

    public LHApiError(String message, LHResponseCode code) {
        this.code = code;
        this.message = message;
    }

    public LHApiError(Throwable cause, String message) {
        this.message = message;
        this.code = LHResponseCode.CONNECTION_ERROR;
        this.parent = cause;
    }

    public LHApiError(Throwable cause, String message, LHResponseCode code) {
        this.message = message;
        this.code = code;
        this.parent = cause;
    }

    public LHResponseCode getCode() {
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
