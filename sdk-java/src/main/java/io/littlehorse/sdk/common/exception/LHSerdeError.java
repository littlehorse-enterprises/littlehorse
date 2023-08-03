package io.littlehorse.sdk.common.exception;

public class LHSerdeError extends Exception {

    private Throwable exn;
    private String message;

    public LHSerdeError(Throwable exn, String message) {
        this.exn = exn;
        this.message = message;
    }

    public LHSerdeError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getParent() {
        return exn;
    }
}
