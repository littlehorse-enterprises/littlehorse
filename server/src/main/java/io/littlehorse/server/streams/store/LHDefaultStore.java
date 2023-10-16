package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
class LHDefaultStore extends ReadOnlyLHDefaultStore implements LHStore {

    private final KeyValueStore<String, Bytes> nativeStore;

    protected LHDefaultStore(KeyValueStore<String, Bytes> nativeStore) {
        super(nativeStore);
        this.nativeStore = nativeStore;
    }

    @Override
    public void delete(Storeable<?> thing) {
        delete(thing.getStoreKey(), thing.getType());
    }

    @Override
    public void put(Storeable<?> thing) {
        String storeKey = thing.getFullStoreKey();
        log.trace("Putting {}", storeKey);
        nativeStore.put(storeKey, new Bytes(thing.toBytes()));
    }

    @Override
    public void delete(String storeKey, StoreableType cls) {
        String fullKey = Storeable.getFullStoreKey(cls, storeKey);
        log.trace("Deleting {}", fullKey);
        nativeStore.delete(fullKey);
    }
}
