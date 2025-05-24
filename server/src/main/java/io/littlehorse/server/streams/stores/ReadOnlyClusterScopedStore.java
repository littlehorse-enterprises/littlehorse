package io.littlehorse.server.streams.stores;

import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.rocksdb.RocksDB;

/**
 * This interface allows you to Read any Storeable object at the cluster level. It does
 * not allow you to read Tenant-Scoped objects.
 */
public interface ReadOnlyClusterScopedStore extends ReadOnlyBaseStore {

    static ReadOnlyClusterScopedStore newInstance(
            /*ReadOnlyKeyValueStore<String, Bytes> nativeStore, */ ExecutionContext ctx, RocksDB db) {
        return new ReadOnlyClusterScopedStoreImpl(/*nativeStore, */ ctx, db);
    }
}
