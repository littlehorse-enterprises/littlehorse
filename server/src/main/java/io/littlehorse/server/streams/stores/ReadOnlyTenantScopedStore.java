package io.littlehorse.server.streams.stores;

import java.util.Optional;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import io.littlehorse.server.streams.topology.core.ExecutionContext;

/**
 * This class allows you to Read any Storeable object at the Tenant level. It does
 * not allow you to read Cluster-Scoped objects.
 */
public class ReadOnlyTenantScopedStore extends ReadOnlyModelStore {

    public ReadOnlyTenantScopedStore(
        ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId, ExecutionContext ctx
    ) {
        super(nativeStore, Optional.of(tenantId), ctx);
    }
}
