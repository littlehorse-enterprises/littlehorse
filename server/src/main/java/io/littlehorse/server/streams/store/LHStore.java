package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public interface LHStore extends ReadOnlyLHStore {

    void delete(Storeable<?> thing);

    void put(Storeable<?> thing);

    void delete(String storeKey, StoreableType cls);

    static LHStore defaultStore(KeyValueStore<String, Bytes> keyValueStore) {
        return new LHDefaultStore(keyValueStore);
    }

    static LHTenantStore tenantStore(KeyValueStore<String, Bytes> keyValueStore, String tenantId) {
        return new LHTenantStore(keyValueStore, tenantId);
    }

    static LHStore defaultStore(ProcessorContext<String, ?> streamsProcessorContext, String storeName) {
        return defaultStore(streamsProcessorContext.getStateStore(storeName));
    }
}
