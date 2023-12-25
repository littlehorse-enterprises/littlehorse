package io.littlehorse.server.streams.stores;

import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * This interface allows you to Read any Storeable object at the Tenant level. It does
 * not allow you to read Cluster-Scoped objects.
 */
public interface ReadOnlyTenantScopedStore extends ReadOnlyBaseStore {

    static ReadOnlyTenantScopedStore newInstance(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext ctx) {
        return new ReadOnlyTenantScopedStoreImpl(nativeStore, tenantId, ctx);
    }
}
