package io.littlehorse.common.model.metadatacommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TenantAdministrationTest {

    @Mock
    private LHServerConfig config;

    @Mock
    private LHServer server;

    private ExecutionContext executionContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);

    private final MetadataCache metadataCache = new MetadataCache();
    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private MetadataProcessor metadataProcessor;

    private final String tenantId = "test-tenant-id";

    private final PutTenantRequestModel putTenantRequest =
            PutTenantRequestModel.fromProto(putTenantRequest(tenantId), PutTenantRequestModel.class, mock());

    private Headers metadata = HeadersUtil.metadataHeadersFor(
            new TenantIdModel(tenantId), new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL));

    private final ReadOnlyMetadataManager metadataManager = new ReadOnlyMetadataManager(
            ClusterScopedStore.newInstance(nativeMetadataStore, executionContext),
            TenantScopedStore.newInstance(nativeMetadataStore, new TenantIdModel("my-tenant"), executionContext),
            metadataCache);
    private final AsyncWaiters asyncWaiters = new AsyncWaiters();

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache, asyncWaiters);
    }

    @Test
    public void supportStoringNewTenant() {
        MetadataCommandModel command = new MetadataCommandModel(putTenantRequest);
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(
                new Record<>(UUID.randomUUID().toString(), command.toProto().build(), 0L, metadata));
        assertThat(storedTenant()).isNotNull();
        assertThat(storedTenant().getObjectId()).isNotNull();
    }

    @Test
    public void shouldUpdateExistingTenant() {
        MetadataCommandModel command = new MetadataCommandModel(putTenantRequest);
        metadataProcessor.init(mockProcessorContext);

        long firstTimestamp = 42L;
        long secondTimestamp = 137L;

        command.setTime(new Date(firstTimestamp));
        metadataProcessor.process(
                new Record<>(UUID.randomUUID().toString(), command.toProto().build(), firstTimestamp, metadata));

        command.setTime(new Date(secondTimestamp));
        command.setCommandId(UUID.randomUUID().toString());
        CompletableFuture<Message> futureResponse = asyncWaiters.getOrRegisterFuture(
                command.getCommandId().get(), Message.class, new CompletableFuture<>());
        metadataProcessor.process(
                new Record<>(UUID.randomUUID().toString(), command.toProto().build(), secondTimestamp, metadata));
        assertThat(futureResponse).isCompleted();
        assertThat(storedTenant()).isNotNull();
        assertThat(storedTenant().getCreatedAt().getTime()).isEqualTo(firstTimestamp);
    }

    @Test
    void shouldThrowExceptionWhenTenantHasABackSlashInvalidCharacter() {
        String invalidTenant = "///Tenant";
        PutTenantRequestModel putTenantRequestModel =
                PutTenantRequestModel.fromProto(putTenantRequest(invalidTenant), PutTenantRequestModel.class, mock());
        MetadataCommandModel command = new MetadataCommandModel(putTenantRequestModel);
        CompletableFuture<Message> futureResponse = asyncWaiters.getOrRegisterFuture(
                command.getCommandId().get(), Message.class, new CompletableFuture<>());
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(
                new Record<>(UUID.randomUUID().toString(), command.toProto().build(), 0L, metadata));
        assertThatThrownBy(() -> futureResponse.getNow(null))
                .hasCauseInstanceOf(LHApiException.class)
                .hasRootCauseMessage("INVALID_ARGUMENT: / and \\ are not valid characters for Tenant");
    }

    @Test
    void shouldThrowExceptionWhenTenantHasAForwardSlashInvalidCharacter() {
        String invalidTenant = "Tenanṭ\\";
        PutTenantRequestModel putTenantRequestModel =
                PutTenantRequestModel.fromProto(putTenantRequest(invalidTenant), PutTenantRequestModel.class, mock());
        MetadataCommandModel command = new MetadataCommandModel(putTenantRequestModel);
        CompletableFuture<Message> futureResponse = asyncWaiters.getOrRegisterFuture(
                command.getCommandId().get(), Message.class, new CompletableFuture<>());
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(
                new Record<>(UUID.randomUUID().toString(), command.toProto().build(), 0L, metadata));

        assertThatThrownBy(() -> futureResponse.getNow(null))
                .hasCauseInstanceOf(LHApiException.class)
                .hasRootCauseMessage("INVALID_ARGUMENT: / and \\ are not valid characters for Tenant");
    }

    private TenantModel storedTenant() {
        return metadataManager.get(new TenantIdModel(tenantId));
    }

    private PutTenantRequest putTenantRequest(String tenantId) {
        return PutTenantRequest.newBuilder().setId(tenantId).build();
    }
}
