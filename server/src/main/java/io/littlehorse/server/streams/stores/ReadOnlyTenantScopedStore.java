package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.rocksdb.RocksDB;

/**
 * This interface allows you to Read any Storeable object at the Tenant level. It does
 * not allow you to read Cluster-Scoped objects.
 */
public interface ReadOnlyTenantScopedStore extends ReadOnlyBaseStore {

    static ReadOnlyTenantScopedStore newInstance(
            /*ReadOnlyKeyValueStore<String, Bytes> nativeStore, */ TenantIdModel tenantId,
            ExecutionContext ctx,
            RocksDB db) {
        return new ReadOnlyTenantScopedStoreImpl(/*nativeStore,*/ tenantId, ctx, db);
    }
}
