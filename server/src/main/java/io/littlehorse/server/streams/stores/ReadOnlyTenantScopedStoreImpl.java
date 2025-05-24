package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.rocksdb.RocksDB;

/**
 * This class allows you to Read any Storeable object at the Tenant level. It does
 * not allow you to read Cluster-Scoped objects.
 */
class ReadOnlyTenantScopedStoreImpl extends ReadOnlyBaseStoreImpl implements ReadOnlyTenantScopedStore {

    public ReadOnlyTenantScopedStoreImpl(
            /*ReadOnlyKeyValueStore<String, Bytes> nativeStore, */ TenantIdModel tenantId,
            ExecutionContext ctx,
            RocksDB db) {
        super(/*nativeStore,*/ tenantId, ctx, db);
    }
}
