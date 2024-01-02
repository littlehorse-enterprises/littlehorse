package io.littlehorse.server.streams.stores;

import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * This class allows you to Read any Storeable object at the cluster level. It does
 * not allow you to read Tenant-Scoped objects.
 */
class ReadOnlyClusterScopedStoreImpl extends ReadOnlyBaseStoreImpl implements ReadOnlyClusterScopedStore {

    public ReadOnlyClusterScopedStoreImpl(ReadOnlyKeyValueStore<String, Bytes> nativeStore, ExecutionContext ctx) {
        super(nativeStore, ctx);
    }
}
