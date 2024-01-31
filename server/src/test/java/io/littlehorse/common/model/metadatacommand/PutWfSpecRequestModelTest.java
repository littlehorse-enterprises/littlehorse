package io.littlehorse.common.model.metadatacommand;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
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
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PutWfSpecRequestModelTest {

    private final MetadataCommand wfSpecToProcess =
            MetadataCommand.newBuilder().setPutWfSpec(testWorkflowSpec()).build();

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();

    private MetadataProcessor metadataProcessor;

    private static final String TENANT_ID_A = "A";
    private static final String TENANT_ID_B = "B";
    private static final String DEFAULT_TENANT_ID = "default";

    private final ExecutionContext executionContext = mock(Answers.RETURNS_DEEP_STUBS);

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();
    private TenantScopedStore defaultStore =
            TenantScopedStore.newInstance(nativeInMemoryStore, new TenantIdModel(DEFAULT_TENANT_ID), executionContext);
    private TenantScopedStore tenantAStore =
            TenantScopedStore.newInstance(nativeInMemoryStore, new TenantIdModel(TENANT_ID_A), executionContext);

    private TenantScopedStore tenantBStore =
            TenantScopedStore.newInstance(nativeInMemoryStore, new TenantIdModel(TENANT_ID_B), executionContext);

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
    }

    @ParameterizedTest
    @ValueSource(strings = {TENANT_ID_A, TENANT_ID_B, DEFAULT_TENANT_ID})
    void supportStoringWfSpecWithTenantIsolation(final String tenantId) {
        TaskDefModel greet = TestUtil.taskDef("greet");
        Headers recordMetadata =
                HeadersUtil.metadataHeadersFor(new TenantIdModel(tenantId), new PrincipalIdModel("my-principal-id"));
        String specName = wfSpecToProcess.getPutWfSpec().getName();
        tenantAStore.put(new StoredGetable<>(greet));
        tenantBStore.put(new StoredGetable<>(greet));
        defaultStore.put(new StoredGetable<>(greet));
        String commandId = UUID.randomUUID().toString();
        metadataProcessor.init(mockProcessorContext);
        metadataProcessor.process(new Record<>(commandId, wfSpecToProcess, 0L, recordMetadata));
        WfSpecIdModel wfSpecToSearch = new WfSpecIdModel(specName, 0, 0);
        if (Objects.equals(tenantId, TENANT_ID_A)) {
            ensureTenantIsolation(wfSpecToSearch, tenantAStore, tenantBStore, defaultStore);
        } else if (Objects.equals(tenantId, TENANT_ID_B)) {
            ensureTenantIsolation(wfSpecToSearch, tenantBStore, tenantAStore, defaultStore);
        } else {
            ensureTenantIsolation(wfSpecToSearch, defaultStore, tenantAStore, tenantBStore);
        }
    }

    void ensureTenantIsolation(
            WfSpecIdModel wfSpecToSearch,
            TenantScopedStore storeUnderTest,
            TenantScopedStore firstIsolatedStore,
            TenantScopedStore secondIsolatedStore) {
        assertThat(storeUnderTest.get(wfSpecToSearch.getStoreableKey(), StoredGetable.class))
                .isNotNull();
        assertThat(firstIsolatedStore.get(wfSpecToSearch.getStoreableKey(), StoredGetable.class))
                .isNull();
        assertThat(secondIsolatedStore.get(wfSpecToSearch.getStoreableKey(), StoredGetable.class))
                .isNull();
    }

    private PutWfSpecRequest testWorkflowSpec() {
        return new WorkflowImpl("example-basic", wf -> {
                    wf.execute("greet");
                })
                .compileWorkflow();
    }
}
