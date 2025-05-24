package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;
import org.rocksdb.RocksDB;

/**
 * This class allows you to Read and Write any Storeable object within the scope of
 * a certain Tenant.
 */
class TenantScopedStoreImpl extends BaseStoreImpl implements TenantScopedStore {

    public TenantScopedStoreImpl(
            /*KeyValueStore<String, Bytes> nativeStore, */ TenantIdModel tenantId, ExecutionContext ctx, RocksDB db) {
        super(/*nativeStore, */ tenantId, ctx, db);
        Objects.requireNonNull(tenantId);
    }
}
