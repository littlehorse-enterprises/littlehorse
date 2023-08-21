package io.littlehorse.common.exceptions;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/*
 * Thin wrapper around grpc's native StatusRuntimeException. Used to communicate
 * to the client that an orzdash occurred.
 */
public class LHApiException extends StatusRuntimeException {

    public LHApiException(Status status) {
        super(status);
    }

    public LHApiException(Status status, String reason) {
        super(status.withDescription(reason));
    }

    public LHApiException(Status status, Throwable cause) {
        super(status.withCause(cause));
    }
}
