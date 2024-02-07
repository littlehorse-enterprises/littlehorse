package io.littlehorse.server.streams.stores;

import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * This interface allows you to Read any Storeable object at the cluster level. It does
 * not allow you to read Tenant-Scoped objects.
 */
public interface ReadOnlyClusterScopedStore extends ReadOnlyBaseStore {

    static ReadOnlyClusterScopedStore newInstance(
            ReadOnlyKeyValueStore<String, Bytes> nativeStore, ExecutionContext ctx, MetadataCache metadataCache) {
        return new ReadOnlyClusterScopedStoreImpl(nativeStore, ctx, metadataCache);
    }
}
