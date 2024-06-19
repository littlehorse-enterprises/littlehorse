package io.littlehorse.common.exceptions;

import io.grpc.Status;

public class UnRescuableThreadRunException extends Exception {

    private String message;

    public UnRescuableThreadRunException(String msg) {
        this.message = msg;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public LHApiException toLHApiException() {
        return new LHApiException(Status.FAILED_PRECONDITION, getMessage());
    }
}
