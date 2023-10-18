package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class LHTenantStore extends ReadOnlyTenantStore implements LHStore {

    private final KeyValueStore<String, Bytes> nativeStore;

    public LHTenantStore(KeyValueStore<String, Bytes> nativeStore, String tenantId) {
        super(nativeStore, tenantId);
        this.nativeStore = nativeStore;
    }

    @Override
    public void delete(Storeable<?> thing) {
        this.delete(thing.getStoreKey(), thing.getType());
    }

    @Override
    public void put(Storeable<?> thing) {
        String s = new String(thing.toBytes());
        nativeStore.put(appendTenantPrefixTo(thing.getFullStoreKey()), new Bytes(thing.toBytes()));
    }

    public void delete(String storeKey, StoreableType cls) {
        nativeStore.delete(appendTenantPrefixTo(Storeable.getFullStoreKey(cls, storeKey)));
    }
}
