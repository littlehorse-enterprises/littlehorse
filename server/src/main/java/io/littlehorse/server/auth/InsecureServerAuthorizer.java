package io.littlehorse.server.auth;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;

public class InsecureServerAuthorizer implements ServerAuthorizer {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return next.startCall(call, headers);
    }

    public static InsecureServerAuthorizer create() {
        return new InsecureServerAuthorizer();
    }
}
