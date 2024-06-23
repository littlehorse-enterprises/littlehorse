package io.littlehorse.server.auth;

import io.grpc.Metadata;
import io.grpc.ServerInterceptor;
import io.littlehorse.common.LHConstants;

public interface ServerAuthorizer extends ServerInterceptor {

    String INTERNAL_PREFIX = "_";

    /**
     * RequestSanitizer will remove this header from client requests.
     */
    Metadata.Key<String> CLIENT_ID = Metadata.Key.of(INTERNAL_PREFIX + "clientId", Metadata.ASCII_STRING_MARSHALLER);

    Metadata.Key<String> TENANT_ID =
            Metadata.Key.of(LHConstants.TENANT_ID_HEADER_NAME, Metadata.ASCII_STRING_MARSHALLER);
}
