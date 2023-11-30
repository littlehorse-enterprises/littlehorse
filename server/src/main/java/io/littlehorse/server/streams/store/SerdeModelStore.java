package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
class SerdeModelStore extends SerdeReadOnlyModelStore implements ModelStore {

    private final KeyValueStore<String, Bytes> nativeStore;

    SerdeModelStore(final KeyValueStore<String, Bytes> nativeStore, ExecutionContext executionContext) {
        super(nativeStore, executionContext);
        this.nativeStore = nativeStore;
    }

    @Override
    public void put(String storeKey, Storeable<?> storeable) {
        log.trace("Putting %s key".formatted(storeKey));
        nativeStore.put(storeKey, new Bytes(storeable.toBytes()));
    }

    @Override
    public void delete(String storeKey, StoreableType cls) {
        log.trace("Deleting %s key".formatted(storeKey));
        nativeStore.delete(storeKey);
    }
}
