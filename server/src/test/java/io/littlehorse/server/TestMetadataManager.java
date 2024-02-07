package io.littlehorse.server;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

public class TestMetadataManager extends MetadataManager {

    public TestMetadataManager(ClusterScopedStore clusterStore, TenantScopedStore tenantStore) {
        super(clusterStore, tenantStore);
    }

    public static TestMetadataManager create(
            KeyValueStore<String, Bytes> nativeMetadataStore,
            String tenantId,
            ExecutionContext executionContext,
            MetadataCache metadataCache) {
        return new TestMetadataManager(
                ClusterScopedStore.newInstance(nativeMetadataStore, executionContext, metadataCache),
                TenantScopedStore.newInstance(
                        nativeMetadataStore, new TenantIdModel(tenantId), executionContext, metadataCache));
    }
}
