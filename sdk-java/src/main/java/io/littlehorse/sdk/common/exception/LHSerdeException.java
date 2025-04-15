package io.littlehorse.sdk.common.exception;

public class LHSerdeException extends RuntimeException {

    private Throwable exn;
    private String message;

    public LHSerdeException(Throwable exn, String message) {
        this.exn = exn;
        this.message = message;
    }

    public LHSerdeException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getParent() {
        return exn;
    }
}
