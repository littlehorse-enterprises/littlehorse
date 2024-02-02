package io.littlehorse.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptors;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.internal.NoopServerCall;
import io.littlehorse.TestUtil;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.server.KafkaStreamsServerImpl;
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
import org.mockito.Mockito;

public class RequestAuthorizerTest {

    private final KafkaStreamsServerImpl server = mock();
    private final KafkaStreams kafkaStreams = mock();
    private final MetadataCache metadataCache = new MetadataCache();
    private final Context.Key<RequestExecutionContext> contextKey = Context.key("test-context-key");
    private LHServerConfig lhConfig = mock();

    private final TestRequestExecutionContext requestContext = TestRequestExecutionContext.create();
    private TestMetadataManager metadataManager =
            TestMetadataManager.create(requestContext.getGlobalMetadataNativeStore(), "my-tenant", requestContext);
    private final RequestAuthorizer requestAuthorizer =
            new RequestAuthorizer(server, contextKey, metadataCache, requestContext::resolveStoreName, lhConfig);
    private ServerCall<Object, Object> mockCall = mock();
    private final Metadata mockMetadata = mock();

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
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn(null);
        startCall();
        assertThat(resolvedAuthContext).isNotNull();
        assertThat(resolvedAuthContext.acls()).hasSize(1);
        assertThat(resolvedAuthContext.acls())
                .containsExactly(
                        inMemoryAnonymousPrincipal.getGlobalAcls().getAcls().toArray(new ServerACLModel[0]));
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo(LHConstants.ANONYMOUS_PRINCIPAL);
    }

    @Test
    public void supportAnonymousPrincipalWhenClientIdIsNotFound() {
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("principal-id");
        startCall();
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo(LHConstants.ANONYMOUS_PRINCIPAL);
        assertThat(resolvedAuthContext.acls())
                .containsAll(inMemoryAnonymousPrincipal.getGlobalAcls().getAcls());
    }

    @Test
    public void supportPrincipalForSpecificTenant() {
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("principal-id");
        when(mockMetadata.get(ServerAuthorizer.TENANT_ID)).thenReturn("my-tenant");
        PrincipalModel newPrincipal = new PrincipalModel();
        newPrincipal.setId(new PrincipalIdModel("principal-id"));
        newPrincipal.setGlobalAcls(TestUtil.singleAdminAcl("name"));
        TenantModel tenant = new TenantModel("my-tenant");
        metadataManager.put(tenant);
        metadataManager.put(newPrincipal);
        MethodDescriptor<Object, Object> mockMethod = mock();
        when(mockCall.getMethodDescriptor()).thenReturn(mockMethod);
        startCall();
        assertThat(resolvedAuthContext.principalId().getId()).isEqualTo("principal-id");
        assertThat(resolvedAuthContext.acls()).containsOnly(TestUtil.adminAcl());
    }

    @Test
    public void supportAnonymousPrincipalWhenPrincipalIdIsNotFound() {
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("principal-id");
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
            metadataManager.put(customTenant);
            metadataManager.put(adminPrincipal);
            metadataManager.put(limitedPrincipal);
        }

        @Test
        public void supportRequestAuthorizationForAdminPrincipals() {
            when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("admin-principal");
            startCall();
            assertThat(resolvedAuthContext).isNotNull();
        }

        @Test
        public void supportRequiredAclValidation() {
            MethodDescriptor<Object, Object> mockMethod = mock();
            when(mockCall.getMethodDescriptor()).thenReturn(mockMethod);
            when(mockMethod.getBareMethodName()).thenReturn("PutTaskDef");
            when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("limited-principal");
            startCall();
            Mockito.verify(mockCall).close(any(), eq(mockMetadata));
        }

        private PrincipalModel buildLimitedPrincipal() {
            PrincipalModel limitedPrincipal = new PrincipalModel();
            limitedPrincipal.setId(new PrincipalIdModel("limited-principal"));
            limitedPrincipal.setPerTenantAcls(Map.of("my-tenant", TestUtil.singleAcl()));
            return limitedPrincipal;
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
            ServerCall stubCall = new NoopServerCall();
            final Metadata mockMetadata = new Metadata();
            mockMetadata.put(ServerAuthorizer.CLIENT_ID, principalId);
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
                assertThat(principalId).isEqualTo(headers.get(ServerAuthorizer.CLIENT_ID));
                return new NoopServerCall.NoopServerCallListener<>();
            });
        }
        return definitionBuilder.build();
    }
}
