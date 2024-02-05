package io.littlehorse.server;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

public class TestMetadataManager extends MetadataManager {

    public TestMetadataManager(ClusterScopedStore clusterStore, TenantScopedStore tenantStore) {
        super(clusterStore, tenantStore);
    }

    public static TestMetadataManager create(
            KeyValueStore<String, Bytes> nativeMetadataStore, String tenantId, ExecutionContext executionContext) {
        return new TestMetadataManager(
                ClusterScopedStore.newInstance(nativeMetadataStore, executionContext),
                TenantScopedStore.newInstance(nativeMetadataStore, new TenantIdModel(tenantId), executionContext));
    }
}
