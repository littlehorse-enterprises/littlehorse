package io.littlehorse.server.streams.stores;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * Package-private class that allows you to read and write storeables at either
 * the tenant or cluster scope.
 */
@Slf4j
abstract class BaseStoreImpl extends ReadOnlyBaseStoreImpl implements BaseStore {

    private final RocksDB db;
    //    private final KeyValueStore<String, Bytes> nativeStore;

    BaseStoreImpl(
            /*KeyValueStore<String, Bytes> nativeStore, */ TenantIdModel tenantId,
            ExecutionContext context,
            RocksDB db) {
        super(/*nativeStore, */ tenantId, context, db);
        //        this.nativeStore = nativeStore;
        this.db = db;
    }

    BaseStoreImpl(/*KeyValueStore<String, Bytes> nativeStore, */ ExecutionContext context, RocksDB db) {
        this(/*nativeStore, */ null, context, db);
    }

    @Override
    public void put(Storeable<?> thing) {
        String key = maybeAddTenantPrefix(thing.getFullStoreKey());
        if (metadataCache != null) {
            metadataCache.evictCache(key);
        }
        try {
            db.put(key.getBytes(), thing.toBytes());
        } catch (RocksDBException e) {
            log.error("Failed to put key [{}] from rocksdb. Message: {}", key, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String storeKey, StoreableType type) {
        String fullKey = maybeAddTenantPrefix(Storeable.getFullStoreKey(type, storeKey));
        if (metadataCache != null) {
            metadataCache.evictCache(fullKey);
        }
        try {
            db.delete(fullKey.getBytes());
        } catch (RocksDBException e) {
            log.error("Failed to delete key [{}] from rocksdb. Message: {}", fullKey, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
