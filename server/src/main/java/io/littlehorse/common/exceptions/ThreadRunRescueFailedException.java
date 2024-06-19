package io.littlehorse.common.exceptions;

import io.grpc.Status;

public class ThreadRunRescueFailedException extends Exception {
    private String message;

    public ThreadRunRescueFailedException(String msg) {
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
