package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * This class allows you to Read and Write any Storeable object within the scope of
 * a certain Tenant.
 */
class TenantScopedStoreImpl extends BaseStoreImpl implements TenantScopedStore {

    public TenantScopedStoreImpl(
            KeyValueStore<String, Bytes> nativeStore, TenantIdModel tenantId, ExecutionContext ctx) {
        super(nativeStore, tenantId, ctx);
        Objects.requireNonNull(tenantId);
    }
}
