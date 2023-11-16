package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

class DefaultModelStore extends ReadOnlyModelDefaultStore implements ModelStore {

    private final SerdeModelStore serdeModelStore;

    DefaultModelStore(KeyValueStore<String, Bytes> nativeStore, ExecutionContext executionContext) {
        super(nativeStore, executionContext);
        this.serdeModelStore = new SerdeModelStore(nativeStore, executionContext);
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
