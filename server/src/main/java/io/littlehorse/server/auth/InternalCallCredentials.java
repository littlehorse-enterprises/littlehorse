package io.littlehorse.server.auth;

import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.concurrent.Executor;

public class InternalCallCredentials extends CallCredentials {
    private final Context.Key<RequestExecutionContext> requestContextKey;

    public InternalCallCredentials(Context.Key<RequestExecutionContext> requestContextKey) {
        this.requestContextKey = requestContextKey;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier metadataApplier) {
        appExecutor.execute(() -> {
            Metadata metadataToPropagate = new Metadata();
            AuthorizationContext currentAuthorization = requestContextKey.get().authorization();
            metadataToPropagate.put(RequestAuthorizer.TENANT_ID, currentAuthorization.tenantId());
            metadataToPropagate.put(RequestAuthorizer.CLIENT_ID, currentAuthorization.principalId());
            try {
                metadataApplier.apply(metadataToPropagate);
            } catch (Exception ex) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(ex));
            }
        });
    }
}
