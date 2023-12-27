package io.littlehorse.server.streams.stores;

import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * This interface allows you to Read or Write any Storeable object at the cluster level.
 * It does not allow you to read or write Tenant-Scoped objects.
 */
public interface ClusterScopedStore extends ReadOnlyClusterScopedStore, BaseStore {

    static ClusterScopedStore newInstance(KeyValueStore<String, Bytes> nativeStore, ExecutionContext ctx) {
        return new ClusterScopedStoreImpl(nativeStore, ctx);
    }
}
