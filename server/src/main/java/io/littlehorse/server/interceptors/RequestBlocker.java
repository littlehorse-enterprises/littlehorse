package io.littlehorse.server.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class RequestBlocker implements ServerInterceptor {

    private ServerInterceptor delegated = new AllowMode();

    public RequestBlocker() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::blockRequests));
    }

    private void blockRequests() {
        delegated = new BlockMode();
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return delegated.interceptCall(call, headers, next);
    }

    private static class AllowMode implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> serverCall,
                Metadata metadata,
                ServerCallHandler<ReqT, RespT> serverCallHandler) {
            return serverCallHandler.startCall(serverCall, metadata);
        }
    }

    private static class BlockMode implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
            call.close(
                    Status.UNAVAILABLE.withDescription("Server is shutting down, no new requests are being accepted"),
                    new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}
