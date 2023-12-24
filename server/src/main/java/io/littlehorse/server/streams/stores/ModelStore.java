package io.littlehorse.server.streams.stores;

import java.util.Optional;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

/**
 * Package-private class that allows you to read and write storeables at either
 * the tenant or cluster scope.
 */
abstract class ModelStore extends ReadOnlyModelStore {

    private final KeyValueStore<String, Bytes> nativeStore;
    
    ModelStore(
        KeyValueStore<String, Bytes> nativeStore, Optional<String> tenantId, ExecutionContext context
    ) {
        super(nativeStore, tenantId, context);
        this.nativeStore = nativeStore;
    }

    public void put(Storeable<?> thing) {
        String key = maybeAddTenantPrefix(thing.getFullStoreKey());
        nativeStore.put(key, new Bytes(thing.toBytes()));
    }

    public void delete(Storeable<?> thing) {
        delete(thing.getStoreKey(), thing.getType());
    }

    public void delete(String storeKey, StoreableType type) {
        String fullKey = maybeAddTenantPrefix(Storeable.getFullStoreKey(type, storeKey));
        nativeStore.delete(fullKey);
    }
}
