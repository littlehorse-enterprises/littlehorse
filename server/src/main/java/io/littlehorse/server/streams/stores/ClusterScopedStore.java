package io.littlehorse.server.streams.stores;

import java.util.Optional;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

/**
 * This class allows you to Read or Write any Storeable object at the cluster level.
 * It does not allow you to read or write Tenant-Scoped objects.
 */
public class ClusterScopedStore extends ModelStore {
    
    public ClusterScopedStore(
        KeyValueStore<String, Bytes> nativeStore, ExecutionContext ctx
    ) {
        super(nativeStore, Optional.empty(), ctx);
    }

}
