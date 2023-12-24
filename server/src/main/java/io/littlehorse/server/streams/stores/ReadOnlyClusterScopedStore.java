package io.littlehorse.server.streams.stores;

import java.util.Optional;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import io.littlehorse.server.streams.topology.core.ExecutionContext;

/**
 * This class allows you to Read any Storeable object at the cluster level. It does
 * not allow you to read Tenant-Scoped objects.
 */
public class ReadOnlyClusterScopedStore extends ReadOnlyModelStore {

    public ReadOnlyClusterScopedStore(
        ReadOnlyKeyValueStore<String, Bytes> nativeStore, ExecutionContext ctx
    ) {
        super(nativeStore, Optional.empty(), ctx);
    }
}
