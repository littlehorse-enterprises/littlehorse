package io.littlehorse.common.model.io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.proto.DeletePrincipalRequest;
import io.littlehorse.common.proto.Principal;
import io.littlehorse.common.proto.PutPrincipalRequest;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Optional;
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
    }

    @Test
    public void supportStorePrincipal() {
        String newPrincipalTenantId = "my-tenant";
        putPrincipalRequest.setTenantId(newPrincipalTenantId);
        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(TestUtil.adminAcl());
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));

        assertThat(storedPrincipal().getTenant().getId()).isEqualTo(newPrincipalTenantId);
    }

    @Test
    public void supportPrincipalInitializationFromCommandMetadata() {
        putPrincipalRequest.setTenantId(null);
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));

        assertThat(storedPrincipal().getTenant().getId()).isEqualTo(tenantId);
    }

    @Test
    public void supportPrincipalOverwrite() {
        ServerACLModel acl = TestUtil.adminAcl();
        acl.setName(Optional.of("acl-before-overwrite"));
        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(acl);
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        acl.setName(Optional.of("acl-after-overwrite"));
        putPrincipalRequest.setOverwrite(true);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        assertThat(storedPrincipal().getAcls()).containsExactly(acl);
    }

    @Test
    public void shouldPreventPrincipalOverwriteIfItIsNotMarkedToOverwrite() {
        ServerACLModel acl = TestUtil.acl();
        acl.setName(Optional.of("acl-before-overwrite"));
        putPrincipalRequest.getAcls().add(acl);
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        acl.setName(Optional.of("acl-after-overwrite"));
        putPrincipalRequest.setOverwrite(false);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        verify(server).sendErrorToClient(eq(command.getCommandId()), any());
    }

    @Test
    public void supportPrincipalDowngrade() {
        String newPrincipalTenantId = "my-tenant";
        putPrincipalRequest.setTenantId(newPrincipalTenantId);
        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(TestUtil.adminAcl());
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        putPrincipalRequest.setId("other-principal");
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        putPrincipalRequest.setId(principalId);
        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(TestUtil.acl());
        putPrincipalRequest.setOverwrite(true);
        metadataProcessor.process(new Record<>(principalId, command, 0L));
        assertThat(storedPrincipal().getAcls()).containsExactly(TestUtil.acl());
    }

    @Test
    public void shouldPreventTenantLockOut() {
        String newPrincipalTenantId = "my-tenant";
        putPrincipalRequest.setTenantId(newPrincipalTenantId);
        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(TestUtil.adminAcl());
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));

        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(TestUtil.acl());
        putPrincipalRequest.setOverwrite(true);
        metadataProcessor.process(new Record<>(principalId, command, 0L));

        verify(server).sendErrorToClient(eq(command.getCommandId()), any());
    }

    @Test
    public void supportPrincipalDeletion() {
        String newPrincipalTenantId = "my-tenant";
        putPrincipalRequest.setTenantId(newPrincipalTenantId);
        putPrincipalRequest.getAcls().clear();
        putPrincipalRequest.getAcls().add(TestUtil.adminAcl());
        MetadataCommandModel command = new MetadataCommandModel(putPrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(principalId, command, 0L));

        assertThat(storedPrincipal()).isNotNull();
        MetadataCommandModel deleteCommand = new MetadataCommandModel(deletePrincipalRequest);
        command.setTenantId(tenantId);
        metadataProcessor.process(new Record<>(principalId, deleteCommand, 0L));
        assertThat(defaultStore.get(new PrincipalIdModel(principalId))).isNull();
    }

    private PutPrincipalRequest principalRequestToProcess() {
        return PutPrincipalRequest.newBuilder()
                .setId(principalId)
                .setOverwrite(false)
                .addAcls(TestUtil.adminAcl().toProto().build())
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
