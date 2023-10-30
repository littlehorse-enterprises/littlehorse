package io.littlehorse.server.auth;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerInterceptor;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;

public interface ServerAuthorizer extends ServerInterceptor {

    Context.Key<PrincipalModel> PRINCIPAL = Context.key("principal");

    Metadata.Key<String> CLIENT_ID = Metadata.Key.of("clientId", Metadata.ASCII_STRING_MARSHALLER);
    Metadata.Key<String> TENANT_ID = Metadata.Key.of("tenantId", Metadata.ASCII_STRING_MARSHALLER);
}
