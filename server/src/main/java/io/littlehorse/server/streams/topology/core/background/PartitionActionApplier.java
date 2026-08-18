package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * The write side of the background-job machinery. Created fresh on every punctuation and handed to
 * each {@link PartitionAction} as it is drained.
 *
 * <p>Every method here touches the RocksDB store or the ProcessorContext directly, so instances of
 * this class must NEVER escape the Kafka Streams thread.
 */
public final class PartitionActionApplier {

    private final ProcessorContext<String, CommandProcessorOutput> ctx;
    private final KeyValueStore<String, Bytes> nativeCoreStore;
    private final BackgroundContext executionContext = new BackgroundContext();
    private final Map<TenantIdModel, TenantScopedStore> tenantStores = new HashMap<>();
    private ClusterScopedStore clusterStore;

    public PartitionActionApplier(
            ProcessorContext<String, CommandProcessorOutput> ctx, KeyValueStore<String, Bytes> nativeCoreStore) {
        this.ctx = ctx;
        this.nativeCoreStore = nativeCoreStore;
    }

    /**
     * A {@code null} tenantId means the cluster-scoped view of the core store (used by partition
     * metrics, counted tags, etc).
     */
    void put(TenantIdModel tenantId, Storeable<?> value) {
        if (tenantId == null) {
            clusterStore().put(value);
        } else {
            tenantStore(tenantId).put(value);
        }
    }

    void delete(TenantIdModel tenantId, Storeable<?> value) {
        if (tenantId == null) {
            clusterStore().delete(value);
        } else {
            tenantStore(tenantId).delete(value);
        }
    }

    void forward(Record<String, CommandProcessorOutput> record) {
        ctx.forward(record);
    }

    private ClusterScopedStore clusterStore() {
        if (clusterStore == null) {
            clusterStore = ClusterScopedStore.newInstance(nativeCoreStore, executionContext);
        }
        return clusterStore;
    }

    private TenantScopedStore tenantStore(TenantIdModel tenantId) {
        return tenantStores.computeIfAbsent(
                tenantId, id -> TenantScopedStore.newInstance(nativeCoreStore, id, executionContext));
    }
}
