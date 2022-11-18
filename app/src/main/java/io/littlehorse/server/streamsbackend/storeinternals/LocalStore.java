package io.littlehorse.server.streamsbackend.storeinternals;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.GETable;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

public class LocalStore extends LocalReadOnlyStore {

    private KeyValueStore<String, Bytes> store;

    public LocalStore(KeyValueStore<String, Bytes> store, LHConfig config) {
        super(store, config);
        this.store = store;
    }

    public void put(GETable<?> thing) {
        String storeKey = StoreUtils.getStoreKey(thing);
        store.put(storeKey, new Bytes(thing.toBytes(config)));
    }
}
