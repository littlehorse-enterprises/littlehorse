package io.littlehorse.server.streams.stores;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Package-private class that allows you to read and write storeables at either
 * the tenant or cluster scope.
 */
abstract class BaseStoreImpl extends ReadOnlyBaseStoreImpl implements BaseStore {

    private final KeyValueStore<String, Bytes> nativeStore;

    BaseStoreImpl(KeyValueStore<String, Bytes> nativeStore, TenantIdModel tenantId, ExecutionContext context) {
        super(nativeStore, tenantId, context);
        this.nativeStore = nativeStore;
    }

    BaseStoreImpl(KeyValueStore<String, Bytes> nativeStore, ExecutionContext context) {
        this(nativeStore, null, context);
    }

    @Override
    public void put(Storeable<?> thing) {
        String key = maybeAddTenantPrefix(thing.getFullStoreKey());
        nativeStore.put(key, new Bytes(thing.toBytes()));
    }

    @Override
    public void delete(String storeKey, StoreableType type) {
        String fullKey = maybeAddTenantPrefix(Storeable.getFullStoreKey(type, storeKey));
        nativeStore.delete(fullKey);
    }
}
