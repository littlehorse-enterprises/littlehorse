package io.littlehorse.common.model.metadatacommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.sdk.common.proto.DeletePrincipalRequest;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.Map;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PrincipalAdministrationTest {

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();
    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private ExecutionContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();

    private MetadataProcessor metadataProcessor;

    private PutPrincipalRequestModel putPrincipalRequest =
            PutPrincipalRequestModel.fromProto(principalRequestToProcess(), PutPrincipalRequestModel.class, mock());

    private DeletePrincipalRequestModel deletePrincipalRequest =
            DeletePrincipalRequestModel.fromProto(deletePrincipalRequest(), DeletePrincipalRequestModel.class, mock());

    private final String tenantId = "test-tenant-id";
    private final String principalId = "test-principal-id";
    private final String requesterId = "principal-requester";

    private final ClusterScopedStore defaultStore =
            ClusterScopedStore.newInstance(nativeMetadataStore, executionContext);

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));
        PrincipalModel requester = new PrincipalModel();
        requester.setId(new PrincipalIdModel("principal-requester"));
        requester.setPerTenantAcls(putPrincipalRequest.getPerTenantAcls());
        requester.setCreatedAt(new Date());
        requester.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("name")));
        defaultStore.put(new StoredGetable<>(requester));
    }

    @Test
    public void supportStorePrincipal() {
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));
        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAcl()));
        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal().getPerTenantAcls().keySet()).containsExactly(tenantId);
        assertThat(storedPrincipal().getGlobalAcls().getAcls()).isEmpty();
    }

    @Test
    public void supportStorePrincipalWithGlobalAcls() {
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));
        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAcl()));
        putPrincipalRequest.setGlobalAcls(TestUtil.singleAcl());
        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal().getPerTenantAcls().keySet()).isNotEmpty();
        assertThat(storedPrincipal().getGlobalAcls().getAcls()).containsExactly(TestUtil.acl());
    }

    @Test
    public void supportPrincipalOverwrite() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        sendCommand(putPrincipalRequest);

        putPrincipalRequest.setPerTenantAcls(
                Map.of(tenantId.toString(), TestUtil.singleAdminAcl("acl-after-overwrite")));
        putPrincipalRequest.setOverwrite(true);

        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal().getPerTenantAcls()).containsOnlyKeys(tenantId);
        ServerACLsModel aclsModel = storedPrincipal().getPerTenantAcls().get(tenantId);
        assertThat(aclsModel.getAcls()).containsExactly(TestUtil.adminAcl("acl-after-overwrite"));
    }

    @Test
    public void shouldPreventPrincipalOverwriteIfItIsNotMarkedToOverwrite() {
        putPrincipalRequest.setPerTenantAcls(
                Map.of(tenantId.toString(), TestUtil.singleAdminAcl("acl-before-overwrite")));
        sendCommand(putPrincipalRequest);
        putPrincipalRequest.setPerTenantAcls(
                Map.of(tenantId.toString(), TestUtil.singleAdminAcl("acl-after-overwrite")));
        putPrincipalRequest.setOverwrite(false);
        metadataCache.clear();
        MetadataCommandModel command = sendCommand(putPrincipalRequest);
        verify(server).sendErrorToClient(eq(command.getCommandId()), any());
    }

    @Test
    public void supportPrincipalDowngrade() {
        String newPrincipalTenantId = "my-tenant";
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        sendCommand(putPrincipalRequest);
        putPrincipalRequest.setId("other-principal");
        sendCommand(putPrincipalRequest);
        putPrincipalRequest.setId(principalId);
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAcl()));
        putPrincipalRequest.setOverwrite(true);
        sendCommand(putPrincipalRequest);
        assertThat(storedPrincipal().getPerTenantAcls().values()).containsExactly(TestUtil.singleAcl());
    }

    @Test
    public void supportPrincipalDeletion() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId.toString(), TestUtil.singleAdminAcl("name")));
        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal()).isNotNull();
        metadataCache.clear();
        sendCommand(deletePrincipalRequest);
        assertThat(defaultStore.get(new PrincipalIdModel(principalId).getStoreableKey(), StoredGetable.class))
                .isNull();
    }

    @Test
    public void shouldRestrictPrincipalCreationToCurrentTenant() {
        defaultStore.put(new StoredGetable<>(new TenantModel("other-tenant")));
        putPrincipalRequest.setPerTenantAcls(
                Map.of(tenantId, TestUtil.singleAdminAcl("name"), "other-tenant", TestUtil.singleAdminAcl("name2")));
        MetadataCommandModel invalidCommand = sendCommand(putPrincipalRequest);
        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(server).sendErrorToClient(eq(invalidCommand.getCommandId()), exceptionArgumentCaptor.capture());
        Exception thrown = exceptionArgumentCaptor.getValue();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(Exception.class)
                .hasMessage("UNAUTHENTICATED: You are not allowed to write over the tenant other-tenant");
    }

    private PutPrincipalRequest principalRequestToProcess() {
        System.out.println("hi there");
        System.out.println(this.principalId);
        return PutPrincipalRequest.newBuilder()
                .setId(principalId)
                .setOverwrite(false)
                .putPerTenantAcls(
                        tenantId.toString(),
                        ServerACLs.newBuilder()
                                .addAcls(TestUtil.acl().toProto())
                                .build())
                .build();
    }

    private DeletePrincipalRequest deletePrincipalRequest() {
        return DeletePrincipalRequest.newBuilder().setId(principalId.toString()).build();
    }

    private PrincipalModel storedPrincipal() {
        metadataCache.clear();
        StoredGetable<Principal, PrincipalModel> storedPrincipal =
                defaultStore.get(new PrincipalIdModel(principalId.toString()).getStoreableKey(), StoredGetable.class);
        assertThat(storedPrincipal).isNotNull();
        return storedPrincipal.getStoredObject();
    }

    private MetadataCommandModel sendCommand(MetadataSubCommand<?> putPrincipalRequest) {
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, requesterId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(requesterId, command.toProto().build(), 0L, metadata));
        return command;
    }
}
