package io.littlehorse.server.auth;

import io.grpc.BindableService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.dao.ServerDAOFactory;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
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
import java.util.concurrent.ConcurrentHashMap;

public class RequestAuthorizer implements ServerAuthorizer {

    private ServerDAOFactory daoFactory;

    private final AclVerifier aclVerifier;
    private ReadOnlyMetadataProcessorDAO dao;

    public static Map<String, String> principalForThread = new ConcurrentHashMap<>();

    public RequestAuthorizer(BindableService service, ServerDAOFactory factory) {
        this.daoFactory = factory;
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
            principalForThread.put(
                    resolvedPrincipal.getId(), Thread.currentThread().getName());
            validateAcl(call.getMethodDescriptor(), resolvedPrincipal, tenantId);
            AuthorizationContext authContext = contextFor(resolvedPrincipal, tenantId);
            context = context.withValue(AUTH_CONTEXT, authContext);
        } catch (PermissionDeniedException ex) {
            call.close(Status.PERMISSION_DENIED.withDescription(ex.getMessage()), headers);
        }
        return Contexts.interceptCall(context, call, headers, next);
    }

    private PrincipalModel resolvePrincipal(String clientId, String tenantId) {
        if (clientId != null && tenantId != null) {
            PrincipalModel storedPrincipal = dao().get(new PrincipalIdModel(clientId));
            if (storedPrincipal == null) {
                return dao().getPrincipal(null);
            }
            return storedPrincipal;
        } else if (clientId != null) {
            PrincipalModel storedPrincipal = dao().get(new PrincipalIdModel(clientId));
            if (storedPrincipal == null) {
                return dao().getPrincipal(null);
            }
            return storedPrincipal;
        } else {
            return dao().getPrincipal(null);
        }
    }

    private void validateAcl(MethodDescriptor<?, ?> method, PrincipalModel principalToValidate, String tenantId) {
        if (!principalToValidate.isAdmin()) {
            ServerACLsModel perTenantAcls =
                    principalToValidate.getPerTenantAcls().get(tenantId);
            if (perTenantAcls != null) {
                aclVerifier.verify(
                        method,
                        principalToValidate.getPerTenantAcls().get(tenantId).getAcls());
            } else {
                aclVerifier.verify(method, principalToValidate.getGlobalAcls().getAcls());
            }
        }
    }

    private AuthorizationContext contextFor(PrincipalModel resolvedPrincipal, String tenantId) {
        if (tenantId == null) {
            tenantId = LHConstants.DEFAULT_TENANT;
        }
        List<ServerACLModel> currentAcls;
        if (resolvedPrincipal.getPerTenantAcls().containsKey(tenantId)) {
            currentAcls = resolvedPrincipal.getPerTenantAcls().get(tenantId).getAcls();
        } else {
            currentAcls = resolvedPrincipal.getGlobalAcls().getAcls();
        }
        return new AuthorizationContextImpl(
                resolvedPrincipal.getId(), tenantId, AuthorizationContext.Scope.READ, currentAcls);
    }

    private ReadOnlyMetadataProcessorDAO dao() {
        dao = dao != null ? dao : daoFactory.getDefaultMetadataDao();
        return dao;
    }

    private static class AclVerifier {

        private final Map<String, AuthMetadata> methodMetadata = new HashMap<>();
        private final List<ACLAction> adminActions = List.of(ACLAction.ALL_ACTIONS);
        private final List<ACLResource> adminResources = List.of(ACLResource.ALL);

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

        private void verify(MethodDescriptor<?, ?> serviceMethod, List<ServerACLModel> acls) {
            String methodName = serviceMethod.getBareMethodName();
            AuthMetadata authMetadata = methodMetadata.get(methodName);
            Set<ACLAction> clientAllowedActions = new HashSet<>();
            Set<ACLResource> clientAllowedResources = new HashSet<>();
            for (ServerACLModel clientAcl : acls) {
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
