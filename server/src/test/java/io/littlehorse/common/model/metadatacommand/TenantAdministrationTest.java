package io.littlehorse.common.model.metadatacommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLsModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.monitoring.metrics.CommandProcessorMetrics;
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
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class TenantAdministrationTest {

    private final String tenantId = "test-tenant-id";
    private final String requesterId = "principal-requester";
    private final LHServerConfig config = mock();
    private final MetadataCache metadataCache = new MetadataCache();
    private final ExecutionContext executionContext = mock(RETURNS_DEEP_STUBS);
    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private final ClusterScopedStore defaultStore =
            ClusterScopedStore.newInstance(nativeMetadataStore, executionContext);
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private final AsyncWaiters asyncWaiters = new AsyncWaiters();
    private MetadataProcessor metadataProcessor;

    @BeforeEach
    void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(
                config, mock(LHServer.class), metadataCache, asyncWaiters, mock(CommandProcessorMetrics.class));
        defaultStore.put(new StoredGetable<>(new TenantModel(tenantId)));

        PrincipalModel requester = new PrincipalModel();
        requester.setId(new PrincipalIdModel(requesterId));
        requester.setCreatedAt(new Date());
        requester.setGlobalAcls(ServerACLsModel.fromProto(
                ServerACLs.newBuilder()
                        .addAcls(ServerACL.newBuilder()
                                .addAllowedActions(ACLAction.WRITE_METADATA)
                                .addResources(ACLResource.ACL_TENANT))
                        .build(),
                ServerACLsModel.class,
                mock()));
        defaultStore.put(new StoredGetable<>(requester));
    }

    @Test
    void shouldCreateTenantWithoutOutputTopicConfig() {
        Tenant response =
                sendCommand(PutTenantRequest.newBuilder().setId("new-tenant").build());

        assertThat(response.getId().getId()).isEqualTo("new-tenant");
        assertThat(response.hasOutputTopicConfig()).isFalse();
        assertThat(storedTenant("new-tenant")).isEqualTo(response);
    }

    @ParameterizedTest
    @EnumSource(
            value = OutputTopicRecordingLevel.class,
            names = {"ALL_ENTITY_EVENTS", "NO_ENTITY_EVENTS"})
    void shouldPreserveOutputTopicConfigWhenOmitted(OutputTopicRecordingLevel recordingLevel) {
        OutputTopicConfig outputConfig = OutputTopicConfig.newBuilder()
                .setDefaultRecordingLevel(recordingLevel)
                .build();
        sendCommand(PutTenantRequest.newBuilder()
                .setId(tenantId)
                .setOutputTopicConfig(outputConfig)
                .build());

        Tenant response = sendCommand(PutTenantRequest.newBuilder()
                .setId(tenantId)
                .setName("Updated tenant")
                .build());

        assertThat(response.getName()).isEqualTo("Updated tenant");
        assertThat(response.hasOutputTopicConfig()).isTrue();
        assertThat(response.getOutputTopicConfig()).isEqualTo(outputConfig);
        assertThat(storedTenant(tenantId)).isEqualTo(response);
    }

    @ParameterizedTest
    @EnumSource(
            value = OutputTopicRecordingLevel.class,
            names = {"ALL_ENTITY_EVENTS", "NO_ENTITY_EVENTS"})
    void shouldReplaceOutputTopicConfigWhenProvided(OutputTopicRecordingLevel recordingLevel) {
        OutputTopicRecordingLevel previousLevel = recordingLevel == OutputTopicRecordingLevel.ALL_ENTITY_EVENTS
                ? OutputTopicRecordingLevel.NO_ENTITY_EVENTS
                : OutputTopicRecordingLevel.ALL_ENTITY_EVENTS;
        sendCommand(PutTenantRequest.newBuilder()
                .setId(tenantId)
                .setOutputTopicConfig(OutputTopicConfig.newBuilder().setDefaultRecordingLevel(previousLevel))
                .build());
        OutputTopicConfig outputConfig = OutputTopicConfig.newBuilder()
                .setDefaultRecordingLevel(recordingLevel)
                .build();

        Tenant response = sendCommand(PutTenantRequest.newBuilder()
                .setId(tenantId)
                .setOutputTopicConfig(outputConfig)
                .build());

        assertThat(response.hasOutputTopicConfig()).isTrue();
        assertThat(response.getOutputTopicConfig()).isEqualTo(outputConfig);
        assertThat(storedTenant(tenantId)).isEqualTo(response);
    }

    @Test
    void shouldPreserveNameWhenOmittedAndClearWhenEmpty() {
        sendCommand(
                PutTenantRequest.newBuilder().setId(tenantId).setName("Acme").build());

        Tenant response =
                sendCommand(PutTenantRequest.newBuilder().setId(tenantId).build());

        assertThat(response.getName()).isEqualTo("Acme");
        assertThat(storedTenant(tenantId)).isEqualTo(response);

        Tenant cleared = sendCommand(
                PutTenantRequest.newBuilder().setId(tenantId).setName("").build());

        assertThat(cleared.hasName()).isFalse();
        assertThat(storedTenant(tenantId)).isEqualTo(cleared);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/Acme", "Acme/Production", "Acme/"})
    void shouldRejectSlashInTenantName(String name) {
        assertThatThrownBy(() -> sendCommand(PutTenantRequest.newBuilder()
                        .setId(tenantId)
                        .setName(name)
                        .build()))
                .hasCauseInstanceOf(LHApiException.class)
                .hasRootCauseMessage("INVALID_ARGUMENT: Tenant name must not contain '/'");
        assertThat(storedTenant(tenantId).hasName()).isFalse();
    }

    private Tenant storedTenant(String id) {
        metadataCache.clear();
        StoredGetable<Tenant, TenantModel> stored =
                defaultStore.get(new TenantIdModel(id).getStoreableKey(), StoredGetable.class);
        assertThat(stored).isNotNull();
        return stored.getStoredObject().toProto().build();
    }

    private Tenant sendCommand(PutTenantRequest request) {
        metadataCache.clear();
        PutTenantRequestModel model =
                PutTenantRequestModel.fromProto(request, PutTenantRequestModel.class, executionContext);
        MetadataCommandModel command = new MetadataCommandModel(model);
        CompletableFuture<Message> response = asyncWaiters.getOrRegisterFuture(
                command.getCommandId().get(), Message.class, new CompletableFuture<>());
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(
                requesterId, command.toProto().build(), 0L, HeadersUtil.metadataHeadersFor(tenantId, requesterId)));
        assertThat(response.isDone()).isTrue();
        return (Tenant) response.join();
    }
}
