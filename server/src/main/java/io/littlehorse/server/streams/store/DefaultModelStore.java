package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
class DefaultModelStore extends ReadOnlyModelDefaultStore implements ModelStore {

    private final SerdeModelStore serdeModelStore;

    DefaultModelStore(KeyValueStore<String, Bytes> nativeStore) {
        super(nativeStore);
        this.serdeModelStore = new SerdeModelStore(nativeStore);
    }

    @Override
    public void put(String storeKey, Storeable<?> storeable) {
        serdeModelStore.put(storeKey, storeable);
    }

    @Override
    public void delete(String storeKey, StoreableType cls) {
        serdeModelStore.delete(Storeable.getFullStoreKey(cls, storeKey), cls);
    }
}
