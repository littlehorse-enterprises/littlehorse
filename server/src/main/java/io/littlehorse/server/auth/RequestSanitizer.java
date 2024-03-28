package io.littlehorse.server.auth;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

public class RequestSanitizer implements ServerAuthorizer {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context context = Context.current();
        headers.discardAll(CLIENT_ID);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
