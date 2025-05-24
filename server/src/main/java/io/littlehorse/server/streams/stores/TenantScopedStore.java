package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.rocksdb.RocksDB;

/**
 * This interface allows you to Read and Write any Storeable object within the scope of
 * a certain Tenant.
 */
public interface TenantScopedStore extends BaseStore, ReadOnlyTenantScopedStore {

    static TenantScopedStore newInstance(
            /*KeyValueStore<String, Bytes> nativeStore, */ TenantIdModel tenantId, ExecutionContext ctx, RocksDB db) {
        return new TenantScopedStoreImpl(/*nativeStore, */ tenantId, ctx, db);
    }
}
