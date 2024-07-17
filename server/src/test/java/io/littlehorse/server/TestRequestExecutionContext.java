package io.littlehorse.server;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
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
    private final MockProcessorContext<String, CommandProcessorOutput> processorContext;

    public TestRequestExecutionContext(
            PrincipalIdModel clientId,
            TenantIdModel tenantId,
            MockProcessorContext<String, CommandProcessorOutput> processorContext,
            KeyValueStore<String, Bytes> globalMetadataNativeStore,
            KeyValueStore<String, Bytes> coreNativeStore,
            MetadataCache metadataCache,
            LHServerConfig lhConfig,
            CoreStoreProvider coreStoreProvider) {
        super(clientId, tenantId, coreStoreProvider, metadataCache, lhConfig);
        this.clientId = clientId.toString();
        this.tenantId = tenantId.toString();
        this.globalMetadataNativeStore = globalMetadataNativeStore;
        this.coreNativeStore = coreNativeStore;
        this.metadataCache = metadataCache;
        this.lhConfig = lhConfig;
        this.processorContext = processorContext;
    }

    public static TestRequestExecutionContext create() {
        final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
        String clientId = "test-client";
        String tenantId = "test-tenant";
        KeyValueStore<String, Bytes> globalMetadataNativeStore =
                TestUtil.testStore(ServerTopology.GLOBAL_METADATA_STORE);
        KeyValueStore<String, Bytes> coreNativeStore = TestUtil.testStore(ServerTopology.CORE_STORE);
        globalMetadataNativeStore.init(mockProcessorContext.getStateStoreContext(), globalMetadataNativeStore);
        coreNativeStore.init(mockProcessorContext.getStateStoreContext(), coreNativeStore);
        MetadataCache metadataCache = new MetadataCache();
        LHServerConfig lhConfig = Mockito.mock();
        CoreStoreProvider mockStoreProvider = Mockito.mock();
        Mockito.when(mockStoreProvider.nativeCoreStore()).thenReturn(coreNativeStore);
        Mockito.when(mockStoreProvider.getNativeGlobalStore()).thenReturn(globalMetadataNativeStore);
        ClusterScopedStore clusterInitStore =
                ClusterScopedStore.newInstance(globalMetadataNativeStore, new BackgroundContext());
        TenantScopedStore coreInitStore =
                TenantScopedStore.newInstance(coreNativeStore, new TenantIdModel(tenantId), new BackgroundContext());
        MetadataManager initManager = new MetadataManager(clusterInitStore, coreInitStore, metadataCache);
        initManager.put(new TenantModel(new TenantIdModel(tenantId)));
        return new TestRequestExecutionContext(
                new PrincipalIdModel(clientId),
                new TenantIdModel(tenantId),
                mockProcessorContext,
                globalMetadataNativeStore,
                coreNativeStore,
                metadataCache,
                lhConfig,
                mockStoreProvider);
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
