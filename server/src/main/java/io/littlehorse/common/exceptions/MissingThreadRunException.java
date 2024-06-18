package io.littlehorse.common.exceptions;

import io.grpc.Status;

public class MissingThreadRunException extends Exception {

    private String message;

    public MissingThreadRunException(String msg) {
        this.message = msg;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public LHApiException toLHApiException() {
        return new LHApiException(Status.INVALID_ARGUMENT, getMessage());
    }
}
