package io.littlehorse.server.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownInterceptor implements ServerInterceptor {

    private static volatile boolean isShuttingDown = false;

    public static void setShuttingDown() {
        log.info("Setting server shutdown state to TRUE, server is shutting down.");
        isShuttingDown = true;
    }

    public static boolean isShuttingDown() {
        return isShuttingDown;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        if (isShuttingDown) {
            call.close(
                    Status.UNAVAILABLE.withDescription("Server is shutting down, no new requests are being accepted"),
                    new Metadata());
            return new ServerCall.Listener<>() {};
        }

        return next.startCall(call, headers);
    }
}
