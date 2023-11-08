package io.littlehorse.common.model.io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.proto.DeletePrincipalRequest;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.PutPrincipalRequest;
import io.littlehorse.common.proto.ServerACLs;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
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
import org.mockito.Mock;
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
    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();

    private MetadataProcessor metadataProcessor;

    private PutPrincipalRequestModel putPrincipalRequest =
            PutPrincipalRequestModel.fromProto(principalRequestToProcess(), PutPrincipalRequestModel.class);

    private DeletePrincipalRequestModel deletePrincipalRequest =
            DeletePrincipalRequestModel.fromProto(deletePrincipalRequest(), DeletePrincipalRequestModel.class);

    private final String tenantId = "test-tenant-id";
    private final String principalId = "test-principal-id";

    private final ModelStore defaultStore = ModelStore.defaultStore(nativeMetadataStore);

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));
    }

    @Test
    public void supportStorePrincipal() {
        String newPrincipalTenantId = "my-tenant";
        defaultStore.put(new StoredGetable<>(new TenantModel(newPrincipalTenantId)));
        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setPerTenantAcls(Map.of(newPrincipalTenantId, TestUtil.singleAcl()));
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));

        assertThat(storedPrincipal().getPerTenantAcls().keySet()).containsExactly(newPrincipalTenantId);
        assertThat(storedPrincipal().getGlobalAcls().getAcls()).isEmpty();
    }

    @Test
    public void supportStorePrincipalWithGlobalAcls() {
        String newPrincipalTenantId = "my-tenant";
        defaultStore.put(new StoredGetable<>(new TenantModel(newPrincipalTenantId)));
        putPrincipalRequest.getPerTenantAcls().clear();
        putPrincipalRequest.setPerTenantAcls(Map.of(newPrincipalTenantId, TestUtil.singleAcl()));

        putPrincipalRequest.setGlobalAcls(TestUtil.singleAcl());
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));

        assertThat(storedPrincipal().getPerTenantAcls().keySet()).isNotEmpty();
        assertThat(storedPrincipal().getGlobalAcls().getAcls()).containsExactly(TestUtil.acl());
    }

    @Test
    public void supportPrincipalOverwrite() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        metadataProcessor.init(mockProcessorContext);

        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));

        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-after-overwrite")));
        putPrincipalRequest.setOverwrite(true);

        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));

        assertThat(storedPrincipal().getPerTenantAcls()).containsOnlyKeys(tenantId);
        ServerACLsModel aclsModel = storedPrincipal().getPerTenantAcls().get(tenantId);
        assertThat(aclsModel.getAcls()).containsExactly(TestUtil.adminAcl("acl-after-overwrite"));
    }

    @Test
    public void shouldPreventPrincipalOverwriteIfItIsNotMarkedToOverwrite() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-after-overwrite")));
        putPrincipalRequest.setOverwrite(false);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));
        verify(server).sendErrorToClient(eq(command.getCommandId()), any());
    }

    @Test
    public void supportPrincipalDowngrade() {
        String newPrincipalTenantId = "my-tenant";
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("acl-before-overwrite")));
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));
        putPrincipalRequest.setId("other-principal");
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));
        putPrincipalRequest.setId(principalId);
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAcl()));
        putPrincipalRequest.setOverwrite(true);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));
        assertThat(storedPrincipal().getPerTenantAcls().values()).containsExactly(TestUtil.singleAcl());
    }

    @Test
    public void supportPrincipalDeletion() {
        putPrincipalRequest.setPerTenantAcls(Map.of(tenantId, TestUtil.singleAdminAcl("name")));
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, principalId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L, metadata));

        assertThat(storedPrincipal()).isNotNull();
        MetadataCommandModel deleteCommand = new MetadataCommandModel(deletePrincipalRequest);
        metadataProcessor.process(new Record<>(principalId, deleteCommand, 0L, metadata));
        assertThat(defaultStore.get(new PrincipalIdModel(principalId))).isNull();
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
        return DeletePrincipalRequest.newBuilder().setId(principalId).build();
    }

    private PrincipalModel storedPrincipal() {
        StoredGetable<Principal, PrincipalModel> storedPrincipal = defaultStore.get(new PrincipalIdModel(principalId));
        assertThat(storedPrincipal).isNotNull();
        return storedPrincipal.getStoredObject();
    }
}
