package io.littlehorse.server.auth;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerInterceptor;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHConstants;

public interface ServerAuthorizer extends ServerInterceptor {

    Context.Key<AuthorizationContext> AUTH_CONTEXT = Context.key("authContext");

    Metadata.Key<String> CLIENT_ID = Metadata.Key.of("clientId", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key<String> TENANT_ID =
            Metadata.Key.of(LHConstants.TENANT_ID_HEADER_NAME, Metadata.ASCII_STRING_MARSHALLER);
}
