package io.littlehorse.server.streams.topology.core.background;

import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
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
 */
public sealed interface PartitionAction {

    /**
     * Applies this action. ONLY ever called on the Kafka Streams thread.
     */
    void apply(PartitionActionApplier applier);

    /**
     * A short description used for logging and metrics.
     */
    String describe();

    /**
     * Writes a Storeable into the core store. A {@code null} tenantId means cluster scope.
     */
    static PartitionAction put(TenantIdModel tenantId, Storeable<?> value) {
        return new Put(tenantId, value);
    }

    /**
     * Deletes a Storeable from the core store. A {@code null} tenantId means cluster scope.
     */
    static PartitionAction delete(TenantIdModel tenantId, Storeable<?> value) {
        return new Delete(tenantId, value);
    }

    /**
     * Forwards a fully-built record downstream. The record must already carry its metadata headers,
     * since the applier has no notion of the tenant/principal that produced it.
     */
    static PartitionAction forward(Record<String, CommandProcessorOutput> record) {
        return new Forward(record);
    }

    record Put(TenantIdModel tenantId, Storeable<?> value) implements PartitionAction {
        @Override
        public void apply(PartitionActionApplier applier) {
            applier.put(tenantId, value);
        }

        @Override
        public String describe() {
            return "PUT " + value.getStoreKey();
        }
    }

    record Delete(TenantIdModel tenantId, Storeable<?> value) implements PartitionAction {
        @Override
        public void apply(PartitionActionApplier applier) {
            applier.delete(tenantId, value);
        }

        @Override
        public String describe() {
            return "DELETE " + value.getStoreKey();
        }
    }

    record Forward(Record<String, CommandProcessorOutput> record) implements PartitionAction {
        @Override
        public void apply(PartitionActionApplier applier) {
            applier.forward(record);
        }

        @Override
        public String describe() {
            return "FORWARD " + record.key();
        }
    }
}
