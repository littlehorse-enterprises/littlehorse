package io.littlehorse.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptors;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.littlehorse.TestUtil;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.server.TestCoreStoreProvider;
import io.littlehorse.server.TestMetadataManager;
import io.littlehorse.server.TestRequestExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class RequestAuthorizerTest {

    private final KafkaStreams kafkaStreams = mock();
    private final MetadataCache metadataCache = new MetadataCache();
    private final Context.Key<RequestExecutionContext> contextKey = Context.key("test-context-key");
    private LHServerConfig lhConfig = mock();

    private final TestRequestExecutionContext requestContext = TestRequestExecutionContext.create();
    private TestMetadataManager metadataManager =
            TestMetadataManager.create(requestContext.getGlobalMetadataNativeStore(), "my-tenant", requestContext);
    private final RequestAuthorizer requestAuthorizer =
            new RequestAuthorizer(contextKey, metadataCache, new TestCoreStoreProvider(requestContext), lhConfig);
    private ServerCall<Object, Object> mockCall = mock(Answers.RETURNS_DEEP_STUBS);
    private final Metadata mockMetadata = mock();
    private final MethodDescriptor methodDescriptor = mock();

    private WfService service = requestContext.service();
    private final ServerServiceDefinition testServiceDefinition = buildTestServiceDefinition(
            ServerServiceDefinition.builder(LittleHorseGrpc.getServiceDescriptor()),
            LittleHorseGrpc.getServiceDescriptor().getMethods());
    private AuthorizationContext resolvedAuthContext;
    private PrincipalModel inMemoryAnonymousPrincipal;

    @BeforeEach
    public void setup() {
        when(kafkaStreams.store(any())).thenReturn(requestContext.getCoreNativeStore());
        inMemoryAnonymousPrincipal = service.getPrincipal(null);
        when(mockCall.getMethodDescriptor().getBareMethodName()).thenReturn("PutPrincipal");
    }

    private void startCall() {
        ServerServiceDefinition intercept = ServerInterceptors.intercept(testServiceDefinition, requestAuthorizer);
        @SuppressWarnings("unchecked")
        ServerMethodDefinition<Object, Object> def =
                (ServerMethodDefinition<Object, Object>) Iterables.get(intercept.getMethods(), 0);
        def.getServerCallHandler().startCall(mockCall, mockMetadata);
    }

    @Test
    public void supportAnonymousPrincipalForDefaultTenant() {
        when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn(null);

        startCall();
        assertThat(resolvedAuthContext).isNotNull();
        assertThat(resolvedAuthContext.acls()).hasSize(1);
        assertThat(resolvedAuthContext.acls())
                .containsExactly(
                        inMemoryAnonymousPrincipal.getGlobalAcls().getAcls().toArray(new ServerACLModel[0]));
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo(LHConstants.ANONYMOUS_PRINCIPAL);
    }

    @Test
    public void supportServerInitialization() {
        when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("principal-id");
        ArgumentCaptor<Status> statusArgumentCaptor = ArgumentCaptor.forClass(Status.class);
        metadataManager.delete(new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL));
        startCall();
        assertThat(contextKey.get()).isNull();
        verify(mockCall).close(statusArgumentCaptor.capture(), same(mockMetadata));
        Status requestStatus = statusArgumentCaptor.getValue();
        assertThat(requestStatus.getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(requestStatus.getDescription()).isEqualTo("Server Initializing");
    }

    @Test
    public void supportAnonymousPrincipalWhenClientIdIsNotFound() {
        when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("principal-id");
        startCall();
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo(LHConstants.ANONYMOUS_PRINCIPAL);
        assertThat(resolvedAuthContext.acls())
                .containsAll(inMemoryAnonymousPrincipal.getGlobalAcls().getAcls());
    }

    @Test
    public void supportPrincipalForSpecificTenant() {
        when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("principal-id");
        when(mockMetadata.get(LHServerInterceptor.TENANT_ID)).thenReturn("my-tenant");
        PrincipalModel newPrincipal = new PrincipalModel();
        newPrincipal.setId(new PrincipalIdModel("principal-id"));
        newPrincipal.setGlobalAcls(TestUtil.singleAdminAcl("name"));
        TenantModel tenant = new TenantModel("my-tenant");
        metadataManager.put(tenant);
        metadataManager.put(newPrincipal);
        startCall();
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo("principal-id");
        assertThat(resolvedAuthContext.acls()).containsOnly(TestUtil.adminAcl());
    }

    @Test
    public void supportPermissionDeniedForNonExistingTenants() {
        when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("principal-id");
        when(mockMetadata.get(LHServerInterceptor.TENANT_ID)).thenReturn("my-missing-tenant");
        PrincipalModel newPrincipal = new PrincipalModel();
        newPrincipal.setId(new PrincipalIdModel("principal-id"));
        newPrincipal.setGlobalAcls(TestUtil.singleAdminAcl("name"));
        metadataManager.put(newPrincipal);
        startCall();
        assertThat(resolvedAuthContext).isNull();
    }

    @Test
    public void supportAnonymousPrincipalWhenPrincipalIdIsNotFound() {
        when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("principal-id");
        metadataManager.put(new TenantModel("my-tenant"));
        startCall();
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo(LHConstants.ANONYMOUS_PRINCIPAL);
        assertThat(resolvedAuthContext.acls())
                .containsOnly(
                        inMemoryAnonymousPrincipal.getGlobalAcls().getAcls().toArray(new ServerACLModel[0]));
    }

    @Nested
    class ACLValidations {

        @BeforeEach
        public void setup() {
            TenantModel customTenant = new TenantModel("my-tenant");
            PrincipalModel adminPrincipal = buildAdminPrincipal();
            PrincipalModel limitedPrincipal = buildLimitedPrincipal();
            PrincipalModel tenantAdminPrincipal = buildTenantAdminPrincipal();
            metadataManager.put(customTenant);
            metadataManager.put(adminPrincipal);
            metadataManager.put(limitedPrincipal);
            metadataManager.put(tenantAdminPrincipal);
        }

        @Test
        public void supportRequestAuthorizationForAdminPrincipals() {
            when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("admin-principal");
            startCall();
            assertThat(resolvedAuthContext).isNotNull();
        }

        @Test
        public void supportRequiredAclValidation() {
            MethodDescriptor<Object, Object> mockMethod = mock();
            when(mockCall.getMethodDescriptor()).thenReturn(mockMethod);
            when(mockMethod.getBareMethodName()).thenReturn("PutTaskDef");
            when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("limited-principal");
            startCall();
            Mockito.verify(mockCall).close(any(), eq(mockMetadata));
        }

        @Test
        public void shouldRejectTenantResourceRequestFromTenantAdminPrincipal() {
            MethodDescriptor<Object, Object> mockMethod = mock();
            when(mockCall.getMethodDescriptor()).thenReturn(mockMethod);
            when(mockMethod.getBareMethodName()).thenReturn("PutTenant");
            when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("tenant-admin-principal");
            startCall();
            ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
            Mockito.verify(mockCall).close(statusCaptor.capture(), eq(mockMetadata));
            assertThat(statusCaptor
                    .getValue()
                    .equals(Status.PERMISSION_DENIED.withDescription(
                            "Missing permissions [WRITE_METADATA] over resources [ACL_TENANT]")));
        }

        @Test
        public void shouldRejectPrincipalResourceRequestFromTenantAdminPrincipal() {
            MethodDescriptor<Object, Object> mockMethod = mock();
            when(mockCall.getMethodDescriptor()).thenReturn(mockMethod);
            when(mockMethod.getBareMethodName()).thenReturn("PutPrincipal");
            when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("tenant-admin-principal");
            startCall();
            ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
            Mockito.verify(mockCall).close(statusCaptor.capture(), eq(mockMetadata));
            assertThat(statusCaptor
                    .getValue()
                    .equals(Status.PERMISSION_DENIED.withDescription(
                            "Missing permissions [WRITE_METADATA] over resources [ACL_PRINCIPAL]")));
        }

        @Test
        public void supportTenantAdmins() {
            MethodDescriptor<Object, Object> mockMethod = mock();
            when(mockCall.getMethodDescriptor()).thenReturn(mockMethod);
            when(mockMethod.getBareMethodName()).thenReturn("PutTaskDef");
            when(mockMetadata.get(LHServerInterceptor.CLIENT_ID)).thenReturn("tenant-admin-principal");
            when(mockMetadata.get(LHServerInterceptor.TENANT_ID)).thenReturn("my-tenant");
            startCall();
            assertThat(resolvedAuthContext).isNotNull();
        }

        private PrincipalModel buildLimitedPrincipal() {
            PrincipalModel limitedPrincipal = new PrincipalModel();
            limitedPrincipal.setId(new PrincipalIdModel("limited-principal"));
            limitedPrincipal.setPerTenantAcls(Map.of("my-tenant", TestUtil.singleAcl()));
            return limitedPrincipal;
        }

        private PrincipalModel buildTenantAdminPrincipal() {
            PrincipalModel tenantAdminPrincipal = new PrincipalModel();
            tenantAdminPrincipal.setId(new PrincipalIdModel("tenant-admin-principal"));
            tenantAdminPrincipal.setPerTenantAcls(Map.of("my-tenant", TestUtil.singleAdminAcl("")));
            return tenantAdminPrincipal;
        }

        private PrincipalModel buildAdminPrincipal() {
            PrincipalModel adminPrincipal = new PrincipalModel();
            adminPrincipal.setId(new PrincipalIdModel("admin-principal"));
            adminPrincipal.setGlobalAcls(TestUtil.singleAdminAcl("name"));
            return adminPrincipal;
        }
    }

    @Test
    public void supportContextPropagation() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        final ServerServiceDefinition serviceDefinition = buildContextPropagationVerifierServiceDefinition(
                ServerServiceDefinition.builder(LittleHorseGrpc.getServiceDescriptor()),
                LittleHorseGrpc.getServiceDescriptor().getMethods());
        final ServerServiceDefinition intercept = ServerInterceptors.intercept(serviceDefinition, requestAuthorizer);
        @SuppressWarnings("unchecked")
        final ServerMethodDefinition<Object, Object> def =
                (ServerMethodDefinition<Object, Object>) Iterables.get(intercept.getMethods(), 0);
        final int numberOfRequests = 10_000;
        Consumer<String> submitCall = principalId -> {
            ServerCall<Object, Object> stubCall = new NoopServerCall<Object, Object>() {
                @Override
                public MethodDescriptor<Object, Object> getMethodDescriptor() {
                    return mockCall.getMethodDescriptor();
                }
            };
            final Metadata mockMetadata = new Metadata();
            mockMetadata.put(LHServerInterceptor.CLIENT_ID, principalId);
            PrincipalModel newPrincipal = new PrincipalModel();
            newPrincipal.setId(new PrincipalIdModel(principalId));
            newPrincipal.setGlobalAcls(TestUtil.singleAdminAcl("name"));
            metadataManager.put(newPrincipal);
            def.getServerCallHandler().startCall(stubCall, mockMetadata);
        };
        List<Future<?>> toDo = new ArrayList<>();
        try {
            for (int i = 0; i < numberOfRequests; i++) {
                String principalId = String.valueOf(i);
                toDo.add(executorService.submit(() -> submitCall.accept(principalId)));
            }
        } finally {
            executorService.shutdown();
            assertThat(executorService.awaitTermination(1, TimeUnit.MINUTES)).isTrue();
            AtomicInteger numberOfFailures = new AtomicInteger();
            List<?> results = toDo.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            numberOfFailures.incrementAndGet();
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .toList();
            assertThat(results).hasSize(numberOfRequests);
            assertThat(numberOfFailures.get()).isZero();
        }
    }

    private ServerServiceDefinition buildTestServiceDefinition(
            ServerServiceDefinition.Builder definitionBuilder, Collection<MethodDescriptor<?, ?>> stubMethods) {
        for (MethodDescriptor<?, ?> method : stubMethods) {
            definitionBuilder = definitionBuilder.addMethod(method, (call, headers) -> {
                if (contextKey.get() != null) {
                    resolvedAuthContext = contextKey.get().authorization();
                }
                return null;
            });
        }
        return definitionBuilder.build();
    }

    private ServerServiceDefinition buildContextPropagationVerifierServiceDefinition(
            ServerServiceDefinition.Builder definitionBuilder, Collection<MethodDescriptor<?, ?>> stubMethods) {
        for (MethodDescriptor<?, ?> method : stubMethods) {
            definitionBuilder = definitionBuilder.addMethod(method, (call, headers) -> {
                String principalId =
                        contextKey.get().authorization().principalId().toString();
                assertThat(principalId).isEqualTo(headers.get(LHServerInterceptor.CLIENT_ID));
                return new NoopServerCallListener<>();
            });
        }
        return definitionBuilder.build();
    }

    // Custom no-op implementations to replace removed gRPC internal classes
    private static class NoopServerCall<ReqT, RespT> extends ServerCall<ReqT, RespT> {
        @Override
        public void request(int numMessages) {}

        @Override
        public void sendHeaders(Metadata headers) {}

        @Override
        public void sendMessage(RespT message) {}

        @Override
        public void close(Status status, Metadata trailers) {}

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {
            return null;
        }
    }

    private static class NoopServerCallListener<ReqT> extends ServerCall.Listener<ReqT> {
        @Override
        public void onMessage(ReqT message) {}

        @Override
        public void onHalfClose() {}

        @Override
        public void onCancel() {}

        @Override
        public void onComplete() {}

        @Override
        public void onReady() {}
    }
}
