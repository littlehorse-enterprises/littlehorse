package io.littlehorse.server.streams.store;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class RocksDBWrapper extends ReadOnlyRocksDBWrapper {

    // note that super has ReadOnlyKeyValueStore<String, Bytes>
    private KeyValueStore<String, Bytes> rocksdb;

    public RocksDBWrapper(KeyValueStore<String, Bytes> rocksdb, LHServerConfig config) {
        super(rocksdb, config);
        this.rocksdb = rocksdb;
    }

    public void delete(Storeable<?> thing) {
        this.delete(thing.getStoreKey(), thing.getType());
    }

    public void put(Storeable<?> thing) {
        String storeKey = thing.getFullStoreKey();
        log.trace("Putting {}", storeKey);
        rocksdb.put(storeKey, new Bytes(thing.toBytes()));
    }

    public void delete(String storeKey, StoreableType cls) {
        String fullKey = Storeable.getFullStoreKey(cls, storeKey);
        log.trace("Deleting {}", fullKey);
        rocksdb.delete(fullKey);
    }
}
