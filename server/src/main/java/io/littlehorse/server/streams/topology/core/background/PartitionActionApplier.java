package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
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
 * this class must NEVER escape the Kafka Streams thread. That restriction is also what makes it
 * valuable: unlike a {@link PartitionJobContext}, reads made through {@link #coreStore()} see the
 * task's own view, including writes not yet committed. Actions that must make a decision against
 * an authoritative view of the store do it here rather than on the worker thread.
 *
 * @param <VOut> output value type of the owning processor.
 */
public final class PartitionActionApplier<VOut> {

    private final ProcessorContext<String, VOut> ctx;
    private final KeyValueStore<String, Bytes> nativeCoreStore;
    private final BackgroundContext executionContext = new BackgroundContext();
    private final Map<TenantIdModel, TenantScopedStore> tenantStores = new HashMap<>();
    private ClusterScopedStore clusterStore;

    public PartitionActionApplier(ProcessorContext<String, VOut> ctx, KeyValueStore<String, Bytes> nativeCoreStore) {
        this.ctx = ctx;
        this.nativeCoreStore = nativeCoreStore;
    }

    /**
     * A {@code null} tenantId means the cluster-scoped view of the core store (used by partition
     * metrics, counted tags, etc).
     */
    public void put(TenantIdModel tenantId, Storeable<?> value) {
        if (tenantId == null) {
            coreStore().put(value);
        } else {
            tenantStore(tenantId).put(value);
        }
    }

    public void delete(TenantIdModel tenantId, Storeable<?> value) {
        if (tenantId == null) {
            coreStore().delete(value);
        } else {
            tenantStore(tenantId).delete(value);
        }
    }

    public void forward(Record<String, ? extends VOut> record) {
        ctx.forward(record);
    }

    /**
     * The cluster-scoped, read-write view of the core store, on a consistent (non-stale) view.
     * Exposed for {@link PartitionAction} implementations outside this package that need to verify
     * something against the Streams thread's own view before taking effect.
     */
    public ClusterScopedStore coreStore() {
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
