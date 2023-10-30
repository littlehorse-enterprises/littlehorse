package io.littlehorse.server.auth;

import io.grpc.BindableService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.dao.ServerDAOFactory;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.ACLAction;
import io.littlehorse.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.server.Authorize;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestAuthorizer implements ServerAuthorizer {

    private final ServerDAOFactory factory;

    private final AclVerifier aclVerifier;

    public RequestAuthorizer(BindableService service, ServerDAOFactory factory) {
        this.factory = factory;
        this.aclVerifier = new AclVerifier(service);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String clientId = headers.get(CLIENT_ID);
        String tenantId = headers.get(TENANT_ID);
        Context context = Context.current();

        try {
            PrincipalModel resolvedPrincipal = resolvePrincipal(clientId, tenantId);
            validateAcl(call.getMethodDescriptor(), resolvedPrincipal);
            context = context.withValue(PRINCIPAL, resolvedPrincipal);
        } catch (PermissionDeniedException ex) {
            call.close(Status.PERMISSION_DENIED.withDescription(ex.getMessage()), headers);
        }
        return Contexts.interceptCall(context, call, headers, next);
    }

    private PrincipalModel resolvePrincipal(String clientId, String tenantId) {
        if (clientId == null && tenantId == null) {
            return PrincipalModel.anonymous();
        } else if (tenantId != null) {
            TenantModel tenant = readOnlyDao().get(new TenantIdModel(tenantId));
            if (tenant == null) {
                throw new PermissionDeniedException("Requested %s tenant does not exist".formatted(tenantId));
            }
            return PrincipalModel.anonymousFor(tenant);
        } else {
            PrincipalModel principal = readOnlyDao().get(new PrincipalIdModel(clientId));
            return principal != null ? principal : PrincipalModel.anonymous();
        }
    }

    private void validateAcl(MethodDescriptor<?, ?> method, PrincipalModel principalToValidate) {
        if (!principalToValidate.isAdmin()) {
            aclVerifier.verify(method, principalToValidate);
        }
    }

    private ReadOnlyMetadataProcessorDAO readOnlyDao() {
        return factory.getDefaultMetadataDao();
    }

    private static class AclVerifier {

        private final Map<String, AuthMetadata> methodMetadata = new HashMap<>();
        private final List<ACLAction> adminActions = List.of(ACLAction.ALL_ACTIONS);
        private final List<ACLResource> adminResources = List.of(ACLResource.ACL_ALL_RESOURCE_TYPES);

        {
            for (MethodDescriptor<?, ?> method :
                    LHPublicApiGrpc.getServiceDescriptor().getMethods()) {
                String methodName = method.getBareMethodName();
                methodMetadata.put(methodName, new AuthMetadata(methodName, adminActions, adminResources));
            }
        }

        private AclVerifier(final BindableService service) {
            this.loadMetadataFromServiceClass(service.getClass());
        }

        private void loadMetadataFromServiceClass(final Class<? extends BindableService> classService) {
            for (Method serviceMethod : classService.getMethods()) {
                if (serviceMethod.isAnnotationPresent(Authorize.class)) {
                    String methodName = serviceMethod.getName();
                    String normalizedMethod = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
                    Authorize authAnnotation = serviceMethod.getAnnotation(Authorize.class);
                    List<ACLAction> requiredActions = List.of(authAnnotation.actions());
                    List<ACLResource> requiredResources = List.of(authAnnotation.resources());
                    if (methodMetadata.containsKey(normalizedMethod)) {
                        methodMetadata.put(
                                normalizedMethod,
                                new AuthMetadata(normalizedMethod, requiredActions, requiredResources));
                    }
                }
            }
        }

        private void verify(MethodDescriptor<?, ?> serviceMethod, PrincipalModel client) {
            String methodName = serviceMethod.getBareMethodName();
            AuthMetadata authMetadata = methodMetadata.get(methodName);
            Set<ACLAction> clientAllowedActions = new HashSet<>();
            Set<ACLResource> clientAllowedResources = new HashSet<>();
            for (ServerACLModel clientAcl : client.getAcls()) {
                clientAllowedActions.addAll(clientAcl.getAllowedActions());
                clientAllowedResources.addAll(clientAcl.getResources());
            }
            if (!(clientAllowedActions.containsAll(authMetadata.requiredActions())
                    && clientAllowedResources.containsAll(authMetadata.requiredResources()))) {
                throw new PermissionDeniedException("Missing permissions %s over resources %s"
                        .formatted(authMetadata.requiredActions(), authMetadata.requiredResources()));
            }
        }

        private record AuthMetadata(
                String methodName, List<ACLAction> requiredActions, List<ACLResource> requiredResources) {}
    }
}
