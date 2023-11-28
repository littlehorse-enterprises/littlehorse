package io.littlehorse.io.littlehorse.common.model.metadatacommand.subcommand;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PutWfSpecRequestModelTest {

    private final MetadataCommandModel wfSpecToProcess = new MetadataCommandModel(testWorkflowSpec());

    @Mock
    private LHServerConfig config;

    @Mock
    private KafkaStreamsServerImpl server;

    private final MetadataCache metadataCache = new MetadataCache();

    private MetadataProcessor metadataProcessor;

    private static final String TENANT_ID_A = "A";
    private static final String TENANT_ID_B = "B";
    private static final String DEFAULT_TENANT_ID = "default";

    private final KeyValueStore<String, Bytes> nativeInMemoryStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();
    private ModelStore defaultStore = ModelStore.instanceFor(nativeInMemoryStore, DEFAULT_TENANT_ID);
    private ModelStore tenantAStore = ModelStore.instanceFor(nativeInMemoryStore, TENANT_ID_A);

    private ModelStore tenantBStore = ModelStore.instanceFor(nativeInMemoryStore, TENANT_ID_B);

    @BeforeEach
    public void setup() {
        nativeInMemoryStore.init(mockProcessorContext.getStateStoreContext(), nativeInMemoryStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
    }

    @ParameterizedTest
    @ValueSource(strings = {TENANT_ID_A, TENANT_ID_B, DEFAULT_TENANT_ID})
    void supportStoringWfSpecWithTenantIsolation(final String tenantId) {
        TaskDefModel greet = TestUtil.taskDef("greet");
        Headers recordMetadata = HeadersUtil.metadataHeadersFor(tenantId, "my-principal-id");
        String specName = wfSpecToProcess.getPutWfSpecRequest().getName();
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
            ModelStore storeUnderTest,
            ModelStore firstIsolatedStore,
            ModelStore secondIsolatedStore) {
        assertThat(storeUnderTest.get(wfSpecToSearch)).isNotNull();
        assertThat(firstIsolatedStore.get(wfSpecToSearch)).isNull();
        assertThat(secondIsolatedStore.get(wfSpecToSearch)).isNull();
    }

    private PutWfSpecRequestModel testWorkflowSpec() {
        return PutWfSpecRequestModel.fromProto(new WorkflowImpl("example-basic", wf -> {
                    wf.execute("greet");
                })
                .compileWorkflow());
    }
}
