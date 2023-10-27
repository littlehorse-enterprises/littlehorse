package io.littlehorse.server.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptors;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.littlehorse.TestUtil;
import io.littlehorse.common.ServerContext;
import io.littlehorse.common.ServerContextImpl;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.dao.ServerDAOFactory;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.MetadataProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RequestAuthorizerTest {

    private final KafkaStreamsServerImpl server = mock();

    private final KafkaStreams kafkaStreams = mock();
    private final MetadataCache metadataCache = new MetadataCache();
    private final ServerContext context = new ServerContextImpl(ModelStore.DEFAULT_TENANT, ServerContext.Scope.READ);

    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.GLOBAL_METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private ModelStore modelStore = ModelStore.defaultStore(nativeMetadataStore);

    private final MetadataProcessorDAO metadataDao = new MetadataProcessorDAOImpl(modelStore, metadataCache, context);

    private final ServerDAOFactory daoFactory = new ServerDAOFactory(kafkaStreams, metadataCache);

    private RequestAuthorizer requestAuthorizer = new RequestAuthorizer(server, daoFactory);

    private ServerCall<Object, Object> mockCall = mock();

    private final Metadata mockMetadata = mock();

    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();

    private final ServerServiceDefinition testServiceDefinition = buildTestServiceDefinition();

    private PrincipalModel resolvedPrincipal;

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        when(kafkaStreams.store(any())).thenReturn(nativeMetadataStore);
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
        Assertions.assertThat(resolvedPrincipal.getAcls()).hasSize(1);
        Assertions.assertThat(resolvedPrincipal.getId()).isEqualTo("anonymous");
        Assertions.assertThat(resolvedPrincipal.isAdmin()).isTrue();
        Assertions.assertThat(resolvedPrincipal.getTenant()).isEqualTo(TenantModel.createDefault());
    }

    @Test
    public void supportPrincipalForDefaultTenant() {
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("principal-id");
        startCall();
        Assertions.assertThat(resolvedPrincipal.getId()).isEqualTo("anonymous");
        Assertions.assertThat(resolvedPrincipal.isAdmin()).isTrue();
        Assertions.assertThat(resolvedPrincipal.getTenant()).isEqualTo(TenantModel.createDefault());
    }

    @Test
    public void supportPrincipalForSpecificTenant() {
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("principal-id");
        TenantModel tenant = new TenantModel("my-tenant");
        List<ServerACLModel> acls = List.of(TestUtil.acl());
        metadataDao.put(tenant);
        metadataDao.put(new PrincipalModel("principal-id", acls, tenant));
        startCall();
        Assertions.assertThat(resolvedPrincipal.getId()).isEqualTo("principal-id");
        Assertions.assertThat(resolvedPrincipal.getAcls()).isEqualTo(acls);
    }

    @Test
    public void supportAnonymousPrincipalWhenPrincipalIdIsNotFound() {
        when(mockMetadata.get(ServerAuthorizer.CLIENT_ID)).thenReturn("principal-id");
        metadataDao.put(new TenantModel("my-tenant"));
        startCall();
        Assertions.assertThat(resolvedPrincipal.getId()).isEqualTo("anonymous");
        Assertions.assertThat(resolvedPrincipal.isAdmin()).isTrue();
    }

    private ServerServiceDefinition buildTestServiceDefinition() {
        ServerServiceDefinition.Builder definitionBuilder =
                ServerServiceDefinition.builder(LHPublicApiGrpc.getServiceDescriptor());
        for (MethodDescriptor<?, ?> method :
                LHPublicApiGrpc.getServiceDescriptor().getMethods()) {
            definitionBuilder = definitionBuilder.addMethod(method, (call, headers) -> {
                resolvedPrincipal = ServerAuthorizer.PRINCIPAL.get();
                return null;
            });
        }
        return definitionBuilder.build();
    }
}
