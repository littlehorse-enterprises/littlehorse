package io.littlehorse.server;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import lombok.Getter;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.mockito.Mockito;

@Getter
public class TestRequestExecutionContext extends RequestExecutionContext {

    private final String clientId;
    private final String tenantId;
    private final ReadOnlyKeyValueStore<String, Bytes> globalMetadataNativeStore;
    private final ReadOnlyKeyValueStore<String, Bytes> coreNativeStore;
    private final MetadataCache metadataCache;
    private final LHServerConfig lhConfig;

    public TestRequestExecutionContext(
            String clientId,
            String tenantId,
            ReadOnlyKeyValueStore<String, Bytes> globalMetadataNativeStore,
            ReadOnlyKeyValueStore<String, Bytes> coreNativeStore,
            MetadataCache metadataCache,
            LHServerConfig lhConfig) {
        super(clientId, tenantId, globalMetadataNativeStore, coreNativeStore, metadataCache, lhConfig);
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.globalMetadataNativeStore = globalMetadataNativeStore;
        this.coreNativeStore = coreNativeStore;
        this.metadataCache = metadataCache;
        this.lhConfig = lhConfig;
    }

    public static TestRequestExecutionContext create() {
        String clientId = "test-client";
        String tenantId = "test-tenant";
        ReadOnlyKeyValueStore<String, Bytes> globalMetadataNativeStore =
                TestUtil.testReadOnlyStore(null, ServerTopology.GLOBAL_METADATA_STORE);
        ReadOnlyKeyValueStore<String, Bytes> coreNativeStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        MetadataCache metadataCache = new MetadataCache();
        LHServerConfig lhConfig = Mockito.mock();
        return new TestRequestExecutionContext(
                clientId, tenantId, globalMetadataNativeStore, coreNativeStore, metadataCache, lhConfig);
    }
}
