package io.littlehorse.common.model.metadatacommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.DeletePrincipalRequest;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.List;
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
    private LHServer server;

    private final MetadataCache metadataCache = new MetadataCache();
    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private ExecutionContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

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
    private final AsyncWaiters asyncWaiters = mock(AsyncWaiters.class);

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache, asyncWaiters);
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));

        PrincipalModel requester = new PrincipalModel();
        requester.setId(new PrincipalIdModel(requesterId));
        requester.setCreatedAt(new Date());
        requester.setGlobalAcls(ServerACLsModel.fromProto(
                ServerACLs.newBuilder()
                        .addAcls(ServerACL.newBuilder()
                                .addAllowedActions(ACLAction.WRITE_METADATA)
                                .addResources(ACLResource.ACL_PRINCIPAL)
                                .build())
                        .build(),
                ServerACLsModel.class,
                mock()));
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
        StoredGetable storedRequester =
                defaultStore.get(new PrincipalIdModel(requesterId).getStoreableKey(), StoredGetable.class);
        PrincipalModel requester = (PrincipalModel) storedRequester.getStoredObject();
        requester.getPerTenantAcls().clear();
        requester.setGlobalAcls(TestUtil.singleAdminAcl("tyler"));
        defaultStore.put(new StoredGetable<>(requester));

        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setGlobalAcls(TestUtil.singleAcl());
        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal().getPerTenantAcls().keySet()).isEmpty();
        assertThat(storedPrincipal().getGlobalAcls().getAcls()).containsExactly(TestUtil.acl());
    }

    @Test
    public void shouldPreventPrivilegeEscalationOnGlobalAcls() {
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));
        StoredGetable storedRequester =
                defaultStore.get(new PrincipalIdModel(requesterId).getStoreableKey(), StoredGetable.class);
        PrincipalModel requester = (PrincipalModel) storedRequester.getStoredObject();
        requester.getPerTenantAcls().clear();
        requester.setGlobalAcls(new ServerACLsModel());
        defaultStore.put(new StoredGetable<>(requester));

        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setGlobalAcls(TestUtil.singleAcl());
        sendCommand(putPrincipalRequest);

        metadataCache.clear();
        StoredGetable<Principal, PrincipalModel> storedPrincipal =
                defaultStore.get(new PrincipalIdModel(principalId.toString()).getStoreableKey(), StoredGetable.class);
        assertThat(storedPrincipal).isNull();
    }

    @Test
    public void supportPrincipalWithoutAcls() {
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));
        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setGlobalAcls(new ServerACLsModel());
        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal().getPerTenantAcls().keySet()).isEmpty();
        assertThat(storedPrincipal().getGlobalAcls().getAcls()).isEmpty();
    }

    @Test
    public void supportPrincipalOverwrite() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        sendCommand(putPrincipalRequest);

        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-after-overwrite")));
        putPrincipalRequest.setOverwrite(true);

        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal().getPerTenantAcls()).containsOnlyKeys(tenantId);
        ServerACLsModel aclsModel = storedPrincipal().getPerTenantAcls().get(tenantId);
        assertThat(aclsModel.getAcls()).containsExactly(TestUtil.adminAcl("acl-after-overwrite"));
    }

    @Test
    public void shouldPreventPrincipalOverwriteIfItIsNotMarkedToOverwrite() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        sendCommand(putPrincipalRequest);
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-after-overwrite")));
        putPrincipalRequest.setOverwrite(false);
        metadataCache.clear();
        MetadataCommandModel command = sendCommand(putPrincipalRequest);
        verify(server).sendErrorToClient(eq(command.getCommandId()), any());
    }

    @Test
    public void supportPrincipalDowngrade() {
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
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("name")));
        sendCommand(putPrincipalRequest);

        assertThat(storedPrincipal()).isNotNull();
        metadataCache.clear();
        sendCommand(deletePrincipalRequest);
        assertThat(defaultStore.get(new PrincipalIdModel(principalId).getStoreableKey(), StoredGetable.class))
                .isNull();
    }

    @Test
    public void shouldAllowPrincipalCreationForAdminPrincipals() {
        defaultStore.put(new StoredGetable<>(new TenantModel("other-tenant")));
        putPrincipalRequest.setPerTenantAcls(
                Map.of(tenantId, TestUtil.singleAdminAcl("name"), "other-tenant", TestUtil.singleAdminAcl("name2")));
        StoredGetable storedRequester =
                defaultStore.get(new PrincipalIdModel(requesterId).getStoreableKey(), StoredGetable.class);
        PrincipalModel requester = (PrincipalModel) storedRequester.getStoredObject();
        requester.getPerTenantAcls().clear();
        requester.setGlobalAcls(TestUtil.singleAdminAcl("tyler"));
        defaultStore.put(new StoredGetable<>(requester));
        sendCommand(putPrincipalRequest);
        verify(server, never()).sendErrorToClient(any(), any());
    }

    @Test
    public void shouldAllowPrincipalCreationForGlobalPrincipalCreators() {
        ServerACLsModel writePrincipalAcl = TestUtil.singleAcl();
        ServerACLModel acl = writePrincipalAcl.getAcls().get(0);
        acl.setAllowedActions(List.of(ACLAction.WRITE_METADATA));
        acl.setResources(List.of(ACLResource.ACL_PRINCIPAL));
        defaultStore.put(new StoredGetable<>(new TenantModel("other-tenant")));
        StoredGetable storedRequester =
                defaultStore.get(new PrincipalIdModel(requesterId).getStoreableKey(), StoredGetable.class);
        PrincipalModel requester = (PrincipalModel) storedRequester.getStoredObject();
        requester.getPerTenantAcls().clear();
        requester.setGlobalAcls(writePrincipalAcl);
        defaultStore.put(new StoredGetable<>(requester));

        putPrincipalRequest.getPerTenantAcls().clear();
        sendCommand(putPrincipalRequest);
        verify(server, never()).sendErrorToClient(any(), any());
    }

    @Test
    void shouldNotAllowPrincipalToHaveAPerTenantACLThatPointsToTenantResource() {
        // Principal with global write permissions over principal metadata tries to create principal with perTenantAcl
        // of Tenant
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAclWithTenantResource()));
        MetadataCommandModel command = sendCommand(putPrincipalRequest);

        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(server).sendErrorToClient(eq(command.getCommandId()), exceptionArgumentCaptor.capture());

        Exception thrown = exceptionArgumentCaptor.getValue();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage(
                        "INVALID_ARGUMENT: PutPrincipalRequest does not allow Per-Tenant ACLs containing permissions over Tenants or Principals.");
    }

    @Test
    void shouldNotAllowPrincipalToHaveAPerTenantACLThatPointsToPrincipalResource() {
        // Principal with global write permissions over principal metadata tries to create principal with perTenantAcl
        // of Principal
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAclWithPrincipalResource()));
        MetadataCommandModel command = sendCommand(putPrincipalRequest);

        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(server).sendErrorToClient(eq(command.getCommandId()), exceptionArgumentCaptor.capture());

        Exception thrown = exceptionArgumentCaptor.getValue();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage(
                        "INVALID_ARGUMENT: PutPrincipalRequest does not allow Per-Tenant ACLs containing permissions over Tenants or Principals.");
    }

    @Test
    public void shouldNotAllowPrincipalWithAllResourcePerTenantACLToAccessPrincipalResource() {
        StoredGetable storedRequester =
                defaultStore.get(new PrincipalIdModel(requesterId).getStoreableKey(), StoredGetable.class);
        PrincipalModel requester = (PrincipalModel) storedRequester.getStoredObject();
        requester.getPerTenantAcls().put(tenantId, TestUtil.singleAdminAcl("test"));
        requester.setGlobalAcls(null);
        defaultStore.put(new StoredGetable<>(requester));

        MetadataCommandModel command = sendCommand(putPrincipalRequest);

        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(server).sendErrorToClient(eq(command.getCommandId()), exceptionArgumentCaptor.capture());

        Exception thrown = exceptionArgumentCaptor.getValue();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("PERMISSION_DENIED: Missing permission WRITE_METADATA over resource ACL_PRINCIPAL.");
    }

    @Test
    public void shouldNotAllowPrincipalWithAllResourcePerTenantACLToAccessTenantResource() {
        StoredGetable storedRequester =
                defaultStore.get(new PrincipalIdModel(requesterId).getStoreableKey(), StoredGetable.class);
        PrincipalModel requester = (PrincipalModel) storedRequester.getStoredObject();
        requester.getPerTenantAcls().put(tenantId, TestUtil.singleAdminAcl("test"));
        requester.setGlobalAcls(null);
        defaultStore.put(new StoredGetable<>(requester));

        PutTenantRequestModel putTenantRequest = PutTenantRequestModel.fromProto(
                PutTenantRequest.newBuilder().setId("test").build(), PutTenantRequestModel.class, mock());

        MetadataCommandModel command = sendCommand(putTenantRequest);

        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(server).sendErrorToClient(eq(command.getCommandId()), exceptionArgumentCaptor.capture());

        Exception thrown = exceptionArgumentCaptor.getValue();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("PERMISSION_DENIED: Missing permission WRITE_METADATA over resource ACL_TENANT.");
    }

    private PutPrincipalRequest principalRequestToProcess() {
        return PutPrincipalRequest.newBuilder()
                .setId(principalId)
                .setOverwrite(false)
                .putPerTenantAcls(
                        tenantId,
                        ServerACLs.newBuilder()
                                .addAcls(TestUtil.acl().toProto())
                                .build())
                .build();
    }

    private DeletePrincipalRequest deletePrincipalRequest() {
        return DeletePrincipalRequest.newBuilder()
                .setId(PrincipalId.newBuilder().setId(principalId))
                .build();
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
