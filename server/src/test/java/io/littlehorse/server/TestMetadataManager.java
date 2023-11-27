package io.littlehorse.server;

import io.littlehorse.server.streams.store.DefaultModelStore;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.TenantModelStore;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

public class TestMetadataManager extends MetadataManager {

    public TestMetadataManager(DefaultModelStore defaultStore, TenantModelStore tenantStore) {
        super(defaultStore, tenantStore);
    }

    public static TestMetadataManager create(
            KeyValueStore<String, Bytes> nativeMetadataStore, String tenantId, ExecutionContext executionContext) {
        return new TestMetadataManager(
                ModelStore.defaultStore(nativeMetadataStore, executionContext),
                ModelStore.tenantStoreFor(nativeMetadataStore, tenantId, executionContext));
    }
}
