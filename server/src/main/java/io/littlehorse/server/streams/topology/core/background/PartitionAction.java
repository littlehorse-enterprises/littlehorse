package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import org.apache.kafka.streams.processor.api.Record;

/**
 * A side effect produced by a {@link PartitionBackgroundJob} running on the per-partition worker
 * thread, which is applied later — in FIFO order — on the Kafka Streams thread by the unified
 * punctuator.
 *
 * <p>This indirection exists because neither RocksDB state stores nor the
 * {@link org.apache.kafka.streams.processor.api.ProcessorContext} are thread safe. Background jobs
 * therefore never mutate state directly: they read through IQv1 and enqueue {@code PartitionAction}s
 * onto the {@link PartitionActionScheduler}, which the punctuator drains.
 *
 * <p>Actions must be small, self-contained and cheap to apply: everything expensive (scans,
 * computation, decision making) belongs on the worker thread.
 *
 * <p>This interface is deliberately NOT sealed. Most jobs only need put/delete/forward, but some
 * need an effect that also has to <i>read</i> a consistent view of the store — see
 * {@code TimerCursorAdvanceAction}, which can only decide how far a cursor may safely move by
 * looking at the Streams thread's own (non-stale) view. Such actions live next to the job that
 * produces them and use {@link PartitionActionApplier#coreStore()}.
 *
 * @param <VOut> output value type of the processor that will apply this action.
 */
public interface PartitionAction<VOut> {

    /**
     * Applies this action. ONLY ever called on the Kafka Streams thread.
     */
    void apply(PartitionActionApplier<VOut> applier);

    /**
     * A short description used for logging and metrics.
     */
    String describe();

    /**
     * Writes a Storeable into the core store. A {@code null} tenantId means cluster scope.
     */
    static <VOut> PartitionAction<VOut> put(TenantIdModel tenantId, Storeable<?> value) {
        return new Put<>(tenantId, value);
    }

    /**
     * Deletes a Storeable from the core store. A {@code null} tenantId means cluster scope.
     */
    static <VOut> PartitionAction<VOut> delete(TenantIdModel tenantId, Storeable<?> value) {
        return new Delete<>(tenantId, value);
    }

    /**
     * Forwards a fully-built record downstream. The record must already carry its metadata headers,
     * since the applier has no notion of the tenant/principal that produced it.
     */
    static <VOut> PartitionAction<VOut> forward(Record<String, ? extends VOut> record) {
        return new Forward<>(record);
    }

    record Put<VOut>(TenantIdModel tenantId, Storeable<?> value) implements PartitionAction<VOut> {
        @Override
        public void apply(PartitionActionApplier<VOut> applier) {
            applier.put(tenantId, value);
        }

        @Override
        public String describe() {
            return "PUT " + value.getStoreKey();
        }
    }

    record Delete<VOut>(TenantIdModel tenantId, Storeable<?> value) implements PartitionAction<VOut> {
        @Override
        public void apply(PartitionActionApplier<VOut> applier) {
            applier.delete(tenantId, value);
        }

        @Override
        public String describe() {
            return "DELETE " + value.getStoreKey();
        }
    }

    record Forward<VOut>(Record<String, ? extends VOut> record) implements PartitionAction<VOut> {
        @Override
        public void apply(PartitionActionApplier<VOut> applier) {
            applier.forward(record);
        }

        @Override
        public String describe() {
            return "FORWARD " + record.key();
        }
    }
}
