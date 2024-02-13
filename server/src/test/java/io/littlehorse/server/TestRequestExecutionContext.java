package io.littlehorse.server;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import lombok.Getter;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.mockito.Mockito;

@Getter
public class TestRequestExecutionContext extends RequestExecutionContext {

    private final String clientId;
    private final String tenantId;
    private final KeyValueStore<String, Bytes> globalMetadataNativeStore;
    private final KeyValueStore<String, Bytes> coreNativeStore;
    private final MetadataCache metadataCache;
    private final LHServerConfig lhConfig;
    private final MockProcessorContext<String, Bytes> processorContext;

    public TestRequestExecutionContext(
            PrincipalIdModel clientId,
            TenantIdModel tenantId,
            MockProcessorContext<String, Bytes> processorContext,
            KeyValueStore<String, Bytes> globalMetadataNativeStore,
            KeyValueStore<String, Bytes> coreNativeStore,
            MetadataCache metadataCache,
            LHServerConfig lhConfig) {
        super(clientId, tenantId, globalMetadataNativeStore, coreNativeStore, metadataCache, lhConfig);
        this.clientId = clientId.toString();
        this.tenantId = tenantId.toString();
        this.globalMetadataNativeStore = globalMetadataNativeStore;
        this.coreNativeStore = coreNativeStore;
        this.metadataCache = metadataCache;
        this.lhConfig = lhConfig;
        this.processorContext = processorContext;
    }

    public static TestRequestExecutionContext create() {
        final MockProcessorContext<String, Bytes> mockProcessorContext = new MockProcessorContext<>();
        String clientId = "test-client";
        String tenantId = "test-tenant";
        KeyValueStore<String, Bytes> globalMetadataNativeStore =
                TestUtil.testStore(ServerTopology.GLOBAL_METADATA_STORE);
        KeyValueStore<String, Bytes> coreNativeStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        globalMetadataNativeStore.init(mockProcessorContext.getStateStoreContext(), globalMetadataNativeStore);
        coreNativeStore.init(mockProcessorContext.getStateStoreContext(), coreNativeStore);
        MetadataCache metadataCache = new MetadataCache();
        LHServerConfig lhConfig = Mockito.mock();
        return new TestRequestExecutionContext(
                new PrincipalIdModel(clientId),
                new TenantIdModel(tenantId),
                mockProcessorContext,
                globalMetadataNativeStore,
                coreNativeStore,
                metadataCache,
                lhConfig);
    }

    public void resetNativeStore() {
        coreNativeStore.init(processorContext.getStateStoreContext(), coreNativeStore);
        globalMetadataNativeStore.init(processorContext.getStateStoreContext(), globalMetadataNativeStore);
    }

    public ReadOnlyKeyValueStore<String, Bytes> resolveStoreName(Integer partition, String storeName) {
        if (storeName.equals(ServerTopology.GLOBAL_METADATA_STORE)) {
            return globalMetadataNativeStore;
        } else if (storeName.equals(ServerTopology.CORE_STORE)) {
            return coreNativeStore;
        } else {
            throw new IllegalArgumentException("%s not supported yet".formatted(storeName));
        }
    }
}
