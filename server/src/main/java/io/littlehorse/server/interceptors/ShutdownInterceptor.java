package io.littlehorse.server.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
public class ShutdownInterceptor implements ServerInterceptor {

    private ServerInterceptor delegated = new OpenGate();

    public ShutdownInterceptor() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::setShuttingDown));
    }

    private void setShuttingDown() {
        delegated = new CloseGate();
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return delegated.interceptCall(call, headers, next);
    }

    private static class OpenGate implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
            return serverCallHandler.startCall(serverCall, metadata);
        }
    }

    private static class CloseGate implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
            call.close(
                    Status.UNAVAILABLE.withDescription("Server is shutting down, no new requests are being accepted"),
                    new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}
