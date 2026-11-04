package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Everything a {@link PartitionBackgroundJob} is allowed to do, and nothing more.
 *
 * <p><b>Reads</b> go through IQv1 ({@link CoreStoreProvider}), pinned to this worker's partition.
 * They are therefore safe to perform off the Kafka Streams thread, at the cost of seeing only
 * committed state — a job may not immediately observe the effect of an action it just staged.
 * Jobs must be written to tolerate that: make them idempotent and resumable (cursor style).
 *
 * <p><b>Writes</b> are not possible. A job {@link #stage(PartitionAction)}s actions and then
 * {@link #submit()}s them; the punctuator applies each submitted batch in full, on the Streams
 * thread, within a single punctuation.
 */
public final class PartitionJobContext<VOut> {

    /**
     * Guards against a job building an atomic unit too large to apply in one punctuation. Exceeding
     * this is a design error in the job — it needs a finer {@link #submit()} granularity — so the run
     * fails loudly rather than putting the Kafka transaction at risk.
     */
    private static final int MAX_BATCH_SIZE = 1_000;

    private final int partition;
    private final LHServerConfig config;
    private final CoreStoreProvider storeProvider;
    private final PartitionActionScheduler<VOut> scheduler;
    private final BackgroundContext executionContext = new BackgroundContext();

    /**
     * Actions staged since the last {@link #submit()}. Only ever touched by the worker thread, and
     * invisible to the punctuator until submitted.
     */
    private final List<PartitionAction<VOut>> stagedBatch = new ArrayList<>();

    /**
     * Normally created by {@link PartitionActionScheduler} for its worker thread. Public so that
     * jobs living in other packages can be unit tested by driving {@code run()} synchronously.
     */
    public PartitionJobContext(
            int partition,
            LHServerConfig config,
            CoreStoreProvider storeProvider,
            PartitionActionScheduler<VOut> scheduler) {
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
     * Number of actions submitted by this worker that the punctuator has not applied yet. Jobs whose
     * next scan must not overlap the effects of the previous one use this as a barrier.
     */
    public int pendingActions() {
        return scheduler.pendingActionCount();
    }

    /**
     * Stages an effect. Nothing is visible to the punctuator, and no backpressure is applied, until
     * {@link #submit()}.
     */
    public void stage(PartitionAction<VOut> action) {
        if (stagedBatch.size() >= MAX_BATCH_SIZE) {
            throw new IllegalStateException("Staged batch exceeded " + MAX_BATCH_SIZE
                    + " actions; this job must call submit() at a finer granularity");
        }
        stagedBatch.add(action);
    }

    public void put(TenantIdModel tenantId, Storeable<?> value) {
        stage(PartitionAction.put(tenantId, value));
    }

    public void delete(TenantIdModel tenantId, Storeable<?> value) {
        stage(PartitionAction.delete(tenantId, value));
    }

    public void forward(Record<String, ? extends VOut> record) {
        stage(PartitionAction.forward(record));
    }

    /**
     * Hands the staged batch to the punctuator, which applies it in full within a single punctuation
     * — and therefore within a single Kafka Streams transaction.
     *
     * <p>Call this wherever the effects staged so far form a consistent unit. A job that never calls
     * it gets exactly one batch per run, which the scheduler submits on its behalf.
     *
     * <p>This is the only blocking call a job makes: it waits while the scheduler's queue is full,
     * which is how backpressure reaches the worker.
     */
    public void submit() throws InterruptedException {
        if (stagedBatch.isEmpty()) {
            return;
        }
        scheduler.submitBatch(List.copyOf(stagedBatch));
        stagedBatch.clear();
    }

    /**
     * Actions staged but not yet submitted. Useful to a job that wants to bound its own unit size,
     * and to tests that drive {@code run()} directly and need to observe progress mid-scan.
     */
    public int stagedCount() {
        return stagedBatch.size();
    }

    /**
     * Called by the scheduler when a run throws. The effects of a failed run are dropped wholesale
     * rather than applied half-finished.
     */
    void discardStaged() {
        stagedBatch.clear();
    }

    private ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore() {
        return storeProvider.nativeCoreStore(partition);
    }
}
