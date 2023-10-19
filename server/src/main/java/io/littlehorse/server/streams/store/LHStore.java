package io.littlehorse.server.streams.store;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import java.util.Objects;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public interface LHStore extends ReadOnlyLHStore {

    void delete(Storeable<?> thing);

    void put(Storeable<?> thing);

    void delete(String storeKey, StoreableType cls);

    static LHStore defaultStore(KeyValueStore<String, Bytes> keyValueStore) {
        return new LHDefaultStore(keyValueStore);
    }

    static ReadOnlyLHStore defaultStore(ReadOnlyKeyValueStore<String, Bytes> keyValueStore) {
        return new ReadOnlyLHDefaultStore(keyValueStore);
    }

    static LHTenantStore tenantStore(KeyValueStore<String, Bytes> keyValueStore, String tenantId) {
        return new LHTenantStore(keyValueStore, tenantId);
    }

    static String tenantIdFor(LHStore store) {
        return store instanceof LHTenantStore ? ((LHTenantStore) store).getTenantId() : "default";
    }

    static LHStore instanceFor(KeyValueStore<String, Bytes> nativeStore, String tenantId) {
        if (Objects.equals(tenantId, "default")) {
            return LHStore.defaultStore(nativeStore);
        } else {
            return LHStore.tenantStore(nativeStore, tenantId);
        }
    }
}
