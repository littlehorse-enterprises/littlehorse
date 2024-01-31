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
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.server.Authorize;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class RequestAuthorizer implements ServerAuthorizer {

    BiFunction<Integer, String, ReadOnlyKeyValueStore<String, Bytes>> storeProvider;

    private final AclVerifier aclVerifier;
    private final Context.Key<RequestExecutionContext> executionContextKey;
    private final MetadataCache metadataCache;
    private final LHServerConfig lhConfig;

    public RequestAuthorizer(
            BindableService service,
            Context.Key<RequestExecutionContext> executionContextKey,
            MetadataCache metadataCache,
            BiFunction<Integer, String, ReadOnlyKeyValueStore<String, Bytes>> storeProvider,
            LHServerConfig lhConfig) {
        this.aclVerifier = new AclVerifier(service);
        this.executionContextKey = executionContextKey;
        this.storeProvider = storeProvider;
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

        Context context = Context.current();
        try {
            RequestExecutionContext requestContext = contextFor(clientId, tenantId);
            validateAcl(call.getMethodDescriptor(), requestContext.authorization());
            context = context.withValue(executionContextKey, requestContext);
        } catch (PermissionDeniedException ex) {
            call.close(Status.PERMISSION_DENIED.withDescription(ex.getMessage()), headers);
        }
        return Contexts.interceptCall(context, call, headers, next);
    }

    private void validateAcl(MethodDescriptor<?, ?> method, AuthorizationContext authContext) {
        if (!authContext.isAdmin()) {
            Collection<ServerACLModel> perTenantAcls = authContext.acls();
            aclVerifier.verify(method, perTenantAcls);
        }
    }

    private RequestExecutionContext contextFor(PrincipalIdModel clientId, TenantIdModel tenantId) {
        return new RequestExecutionContext(
                clientId,
                tenantId,
                storeProvider.apply(null, ServerTopology.GLOBAL_METADATA_STORE),
                storeProvider.apply(null, ServerTopology.CORE_STORE),
                metadataCache,
                lhConfig);
    }

    private static class AclVerifier {

        private final Map<String, AuthMetadata> methodMetadata = new HashMap<>();
        private final List<ACLAction> adminActions = List.of(ACLAction.ALL_ACTIONS);
        private final List<ACLResource> adminResources = List.of(ACLResource.ACL_ALL_RESOURCES);

        {
            for (MethodDescriptor<?, ?> method :
                    LittleHorseGrpc.getServiceDescriptor().getMethods()) {
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

        private void verify(MethodDescriptor<?, ?> serviceMethod, Collection<ServerACLModel> acls) {
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
