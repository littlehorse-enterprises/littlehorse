package io.littlehorse.server.auth.authenticators;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.littlehorse.common.LHConstants;
import io.littlehorse.server.auth.LHServerInterceptor;

/**
 * Authenticator for insecure server listeners. Sets the principal id to `anonymous` for all requests.
 */
public class InsecureAuthenticator implements LHServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        headers.put(CLIENT_ID, LHConstants.ANONYMOUS_PRINCIPAL);
        return next.startCall(call, headers);
    }

    public static InsecureAuthenticator create() {
        return new InsecureAuthenticator();
    }
}
