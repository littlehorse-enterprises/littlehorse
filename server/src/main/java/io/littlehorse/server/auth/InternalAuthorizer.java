package io.littlehorse.server.auth;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Objects;

public class InternalAuthorizer implements ServerAuthorizer {

    private final Context.Key<RequestExecutionContext> executionContextKey;
    private final CoreStoreProvider coreStoreProvider;
    private final MetadataCache metadataCache;
    private final LHServerConfig lhConfig;

    public InternalAuthorizer(
            Context.Key<RequestExecutionContext> executionContextKey,
            CoreStoreProvider coreStoreProvider,
            MetadataCache metadataCache,
            LHServerConfig lhConfig) {
        this.executionContextKey = executionContextKey;
        this.coreStoreProvider = coreStoreProvider;
        this.metadataCache = metadataCache;
        this.lhConfig = lhConfig;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String clientIdStr = headers.get(CLIENT_ID);
        PrincipalIdModel clientId = clientIdStr == null
                ? null
                : (PrincipalIdModel) ObjectIdModel.fromString(clientIdStr.trim(), PrincipalIdModel.class);
        String tenantIdStr = headers.get(TENANT_ID);
        TenantIdModel tenantId = tenantIdStr == null
                ? null
                : (TenantIdModel) ObjectIdModel.fromString(tenantIdStr.trim(), TenantIdModel.class);
        Objects.requireNonNull(clientId);
        Objects.requireNonNull(tenantId);
        RequestExecutionContext requestContext =
                new RequestExecutionContext(clientId, tenantId, coreStoreProvider, metadataCache, lhConfig);
        Context context = Context.current();
        context = context.withValue(executionContextKey, requestContext);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
