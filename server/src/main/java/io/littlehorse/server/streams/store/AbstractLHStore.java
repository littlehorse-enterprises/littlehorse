package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

abstract class AbstractLHStore extends ReadOnlyLHDefaultStore implements LHStore {

    private final KeyValueStore<String, Bytes> nativeStore;

    protected AbstractLHStore(KeyValueStore<String, Bytes> nativeStore) {
        super(nativeStore);
        this.nativeStore = nativeStore;
    }

    public void put(String storeKey, Storeable<?> thing) {
        nativeStore.put(storeKey, new Bytes(thing.toBytes()));
    }

    public void delete(String storeKey) {
        nativeStore.delete(storeKey);
    }
}
