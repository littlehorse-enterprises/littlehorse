package io.littlehorse.server.streams.store;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class LHTenantStore extends AbstractLHStore implements LHStore {

    public LHTenantStore(KeyValueStore<String, Bytes> rocksdb, LHServerConfig config, String tenantId) {
        super(rocksdb, config, tenantId);
    }

    @Override
    public void delete(Storeable<?> thing) {
        this.delete(thing.getStoreKey(), thing.getType());
    }

    @Override
    public void put(Storeable<?> thing) {
        String storeKey = tenantId + "/" + thing.getFullStoreKey();
        log.trace("Putting {}", storeKey);
        put(storeKey, thing);
    }

    public void delete(String storeKey, StoreableType cls) {
        String fullKey = tenantId + "/" + Storeable.getFullStoreKey(cls, storeKey);
        log.trace("Deleting {}", fullKey);
        delete(fullKey);
    }
}
