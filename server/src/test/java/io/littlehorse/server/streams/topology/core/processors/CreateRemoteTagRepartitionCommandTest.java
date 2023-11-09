package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateRemoteTagRepartitionCommandTest {

    private final Tag tagToStore = TestUtil.tag();
    private final String commandId = UUID.randomUUID().toString();

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.CORE_REPARTITION_STORE),
                    Serdes.String(),
                    Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<Void, Void> mockProcessorContext = new MockProcessorContext<>();

    private RepartitionCommandProcessor commandProcessor;

    private static final String TENANT_ID_A = "A", TENANT_ID_B = "B", DEFAULT_TENANT = "default";

    private ModelStore tenantAStore = ModelStore.instanceFor(nativeInMemoryStore, TENANT_ID_A);
    private ModelStore tenantBStore = ModelStore.instanceFor(nativeInMemoryStore, TENANT_ID_B);
    private ModelStore defaultStore = ModelStore.instanceFor(nativeInMemoryStore, DEFAULT_TENANT);

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        commandProcessor = new RepartitionCommandProcessor(config);
    }

    @ParameterizedTest
    @ValueSource(strings = {TENANT_ID_A, TENANT_ID_B, DEFAULT_TENANT})
    void shouldStoreRemoteTagWithTenantIsolation(final String tenantId) {
        RepartitionCommand commandToProcess =
                new RepartitionCommand(new CreateRemoteTag(tagToStore), new Date(), commandId);
        Headers metadata = HeadersUtil.metadataHeadersFor(tenantId, "my-principal-id");
        commandProcessor.init(mockProcessorContext);
        commandProcessor.process(new Record<>(commandId, commandToProcess, 0L, metadata));
        assertThat(mockProcessorContext.scheduledPunctuators()).hasSize(1);
        if (Objects.equals(tenantId, TENANT_ID_A)) {
            ensureTenantIsolation(tagToStore.getStoreKey(), tenantAStore, tenantBStore, defaultStore);
        } else if (Objects.equals(tenantId, TENANT_ID_B)) {
            ensureTenantIsolation(tagToStore.getStoreKey(), tenantBStore, tenantAStore, defaultStore);
        } else {
            ensureTenantIsolation(tagToStore.getStoreKey(), defaultStore, tenantAStore, tenantBStore);
        }
    }

    void ensureTenantIsolation(
            String tagStoreKey,
            ModelStore storeUnderTest,
            ModelStore firstIsolatedStore,
            ModelStore secondIsolatedStore) {
        assertThat(storeUnderTest.get(tagStoreKey, Tag.class)).isNotNull();
        assertThat(firstIsolatedStore.get(tagStoreKey, Tag.class)).isNull();
        assertThat(secondIsolatedStore.get(tagStoreKey, Tag.class)).isNull();
    }
}
