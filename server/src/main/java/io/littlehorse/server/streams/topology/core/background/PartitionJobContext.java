package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Everything a {@link PartitionBackgroundJob} is allowed to do, and nothing more.
 *
 * <p><b>Reads</b> go through IQv1 ({@link CoreStoreProvider}), pinned to this worker's partition.
 * They are therefore safe to perform off the Kafka Streams thread, at the cost of seeing only
 * committed state — a job may not immediately observe the effect of an action it just scheduled.
 * Jobs must be written to tolerate that: make them idempotent and resumable (cursor style).
 *
 * <p><b>Writes</b> are not possible. A job can only {@link #schedule(PartitionAction)} an action,
 * which the punctuator later applies on the Streams thread.
 */
public final class PartitionJobContext {

    private final int partition;
    private final LHServerConfig config;
    private final CoreStoreProvider storeProvider;
    private final PartitionActionScheduler scheduler;
    private final BackgroundContext executionContext = new BackgroundContext();

    /**
     * Normally created by {@link PartitionActionScheduler} for its worker thread. Public so that
     * jobs living in other packages can be unit tested by driving {@code run()} synchronously.
     */
    public PartitionJobContext(
            int partition, LHServerConfig config, CoreStoreProvider storeProvider, PartitionActionScheduler scheduler) {
        this.partition = partition;
        this.config = config;
        this.storeProvider = storeProvider;
        this.scheduler = scheduler;
    }

    public int partition() {
        return partition;
    }

    public LHServerConfig config() {
        return config;
    }

    /**
     * Cluster-scoped read view of the core store for THIS partition.
     */
    public ReadOnlyClusterScopedStore coreStore() {
        return ReadOnlyClusterScopedStore.newInstance(nativeCoreStore(), executionContext);
    }

    /**
     * Tenant-scoped read view of the core store for THIS partition.
     */
    public ReadOnlyTenantScopedStore coreStore(TenantIdModel tenantId) {
        return ReadOnlyTenantScopedStore.newInstance(nativeCoreStore(), tenantId, executionContext);
    }

    /**
     * Cluster-scoped read view of the global metadata store (fully replicated, so no partition
     * pinning needed).
     */
    public ReadOnlyClusterScopedStore metadataStore() {
        return ReadOnlyClusterScopedStore.newInstance(storeProvider.getNativeGlobalStore(), executionContext);
    }

    public ReadOnlyTenantScopedStore metadataStore(TenantIdModel tenantId) {
        return ReadOnlyTenantScopedStore.newInstance(storeProvider.getNativeGlobalStore(), tenantId, executionContext);
    }

    /**
     * Enqueues an effect to be applied by the next punctuation. Blocks if the queue is full, which
     * is how backpressure reaches the job: the worker thread cannot outrun the Streams thread.
     */
    public void schedule(PartitionAction action) throws InterruptedException {
        scheduler.enqueue(action);
    }

    public void put(TenantIdModel tenantId, Storeable<?> value) throws InterruptedException {
        schedule(PartitionAction.put(tenantId, value));
    }

    public void delete(TenantIdModel tenantId, Storeable<?> value) throws InterruptedException {
        schedule(PartitionAction.delete(tenantId, value));
    }

    public void forward(Record<String, CommandProcessorOutput> record) throws InterruptedException {
        schedule(PartitionAction.forward(record));
    }

    /**
     * Same as {@link #forward}, for call sites that cannot declare {@link InterruptedException} —
     * for example a {@code Consumer} handed to shared model code. The scheduler unwraps the
     * resulting {@link UncheckedInterruptedException} so revocation still unwinds cleanly.
     */
    public void forwardUnchecked(Record<String, CommandProcessorOutput> record) {
        try {
            forward(record);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptedException(e);
        }
    }

    private ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore() {
        return storeProvider.nativeCoreStore(partition);
    }
}
