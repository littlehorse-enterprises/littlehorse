package io.littlehorse.server.auth;

import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;

public class ResponseStatusException extends RuntimeException {

    public ResponseStatusException(String message, HttpResponseStatus status) {
        super(
            String.format(
                "%s [code=%s, status=%s]",
                message,
                status.code(),
                status.reasonPhrase()
            )
        );
    }
}
