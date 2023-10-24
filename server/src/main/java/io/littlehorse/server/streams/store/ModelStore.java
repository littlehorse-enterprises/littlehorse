package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import java.util.Objects;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public interface ModelStore extends ReadOnlyModelStore {

    default void delete(Storeable<?> thing) {
        this.delete(thing.getStoreKey(), thing.getType());
    }

    default void put(Storeable<?> storeable) {
        put(storeable.getFullStoreKey(), storeable);
    }

    void put(String storeKey, Storeable<?> storeable);

    void delete(String storeKey, StoreableType cls);

    static ModelStore defaultStore(KeyValueStore<String, Bytes> keyValueStore) {
        return new DefaultModelStore(keyValueStore);
    }

    static ReadOnlyModelStore defaultStore(ReadOnlyKeyValueStore<String, Bytes> keyValueStore) {
        return new ReadOnlyModelDefaultStore(keyValueStore);
    }

    static ModelStore instanceFor(KeyValueStore<String, Bytes> nativeStore, String tenantId) {
        if (Objects.equals(tenantId, "default")) {
            return ModelStore.defaultStore(nativeStore);
        } else {
            return new TenantModelStore(nativeStore, tenantId);
        }
    }

    static ReadOnlyModelStore instanceFor(ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId) {
        if (Objects.equals(tenantId, "default")) {
            return ModelStore.defaultStore(nativeStore);
        } else {
            return new ReadOnlyTenantStore(nativeStore, tenantId);
        }
    }
}
