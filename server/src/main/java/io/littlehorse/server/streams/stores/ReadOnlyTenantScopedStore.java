package io.littlehorse.server.streams.stores;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * This interface allows you to Read any Storeable object at the Tenant level. It does
 * not allow you to read Cluster-Scoped objects.
 */
public interface ReadOnlyTenantScopedStore extends ReadOnlyBaseStore {

    static ReadOnlyTenantScopedStore newInstance(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore,
            TenantIdModel tenantId,
            ExecutionContext ctx,
            MetadataCache metadataCache) {
        return new ReadOnlyTenantScopedStoreImpl(nativeStore, tenantId, ctx, metadataCache);
    }
}
