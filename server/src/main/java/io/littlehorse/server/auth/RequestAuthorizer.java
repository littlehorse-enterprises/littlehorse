package io.littlehorse.server.auth;

import io.grpc.BindableService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.dao.ServerDAOFactory;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;

public class RequestAuthorizer implements ServerAuthorizer {

    private final BindableService service;
    private final ServerDAOFactory factory;

    public RequestAuthorizer(BindableService service, ServerDAOFactory factory) {
        this.service = service;
        this.factory = factory;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String clientId = headers.get(CLIENT_ID);
        String tenantId = headers.get(TENANT_ID);
        Context current = Context.current();
        current = current.withValue(PRINCIPAL, resolvePrincipal(clientId, tenantId));
        return Contexts.interceptCall(current, call, headers, next);
    }

    private PrincipalModel resolvePrincipal(String clientId, String tenantId) {
        if (clientId == null && tenantId == null) {
            return PrincipalModel.anonymous();
        } else if (clientId == null) {
            TenantModel tenant = readOnlyDao().get(new TenantIdModel(tenantId));
            return PrincipalModel.anonymousFor(tenant);
        } else if (tenantId == null) {
            return PrincipalModel.anonymous();
        } else {
            PrincipalModel principalModel = readOnlyDao().get(new PrincipalIdModel(clientId));
            if (principalModel == null) {
                TenantModel tenant = readOnlyDao().get(new TenantIdModel(tenantId));
                return PrincipalModel.anonymousFor(tenant);
            }
            return principalModel;
        }
    }

    private ReadOnlyMetadataProcessorDAO readOnlyDao() {
        return factory.getDefaultMetadataDao();
    }
}
