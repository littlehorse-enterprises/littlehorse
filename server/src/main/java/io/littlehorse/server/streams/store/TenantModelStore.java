package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

class TenantModelStore extends ReadOnlyTenantStore implements ModelStore {

    private final SerdeModelStore serdeModelStore;

    TenantModelStore(KeyValueStore<String, Bytes> nativeStore, String tenantId) {
        super(nativeStore, tenantId);
        this.serdeModelStore = new SerdeModelStore(nativeStore);
    }

    @Override
    public void put(Storeable<?> thing) {
        serdeModelStore.put(appendTenantPrefixTo(thing.getFullStoreKey()), thing);
    }

    @Override
    public void put(String storeKey, Storeable<?> storeable) {
        serdeModelStore.put(appendTenantPrefixTo(storeKey), storeable);
    }

    @Override
    public void delete(String storeKey, StoreableType cls) {
        serdeModelStore.delete(appendTenantPrefixTo(Storeable.getFullStoreKey(cls, storeKey)), cls);
    }
}
