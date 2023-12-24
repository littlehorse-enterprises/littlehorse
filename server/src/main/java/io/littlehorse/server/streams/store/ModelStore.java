package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Deprecated(forRemoval = true)
public interface ModelStore extends ReadOnlyModelStore {

    default void delete(Storeable<?> thing) {
        this.delete(thing.getStoreKey(), thing.getType());
    }

    default void put(Storeable<?> storeable) {
        put(storeable.getFullStoreKey(), storeable);
    }

    void put(String storeKey, Storeable<?> storeable);

    void delete(String storeKey, StoreableType cls);

    static DefaultModelStore defaultStore(
            KeyValueStore<String, Bytes> keyValueStore, ExecutionContext executionContext) {
        return new DefaultModelStore(keyValueStore, executionContext);
    }

    static ReadOnlyModelDefaultStore defaultStore(
            ReadOnlyKeyValueStore<String, Bytes> keyValueStore, ExecutionContext executionContext) {
        return new ReadOnlyModelDefaultStore(keyValueStore, executionContext);
    }

    static ModelStore instanceFor(
            KeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        return new TenantModelStore(nativeStore, tenantId, executionContext);
    }

    static TenantModelStore tenantStoreFor(
            KeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        return new TenantModelStore(nativeStore, tenantId, executionContext);
    }

    static ReadOnlyTenantStore tenantStoreFor(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        return new ReadOnlyTenantStore(nativeStore, tenantId, executionContext);
    }

    static ReadOnlyModelStore instanceFor(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext executionContext) {
        return new ReadOnlyTenantStore(nativeStore, tenantId, executionContext);
    }
}
