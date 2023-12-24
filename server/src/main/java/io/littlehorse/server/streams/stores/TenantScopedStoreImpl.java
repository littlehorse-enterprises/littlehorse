package io.littlehorse.server.streams.stores;

import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * This class allows you to Read and Write any Storeable object within the scope of
 * a certain Tenant.
 */
class TenantScopedStoreImpl extends BaseStoreImpl implements TenantScopedStore {

    public TenantScopedStoreImpl(KeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext ctx) {
        super(nativeStore, Optional.of(tenantId), ctx);
    }
}
