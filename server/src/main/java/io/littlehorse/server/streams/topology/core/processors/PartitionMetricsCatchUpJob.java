package io.littlehorse.server.streams.topology.core.processors;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static com.google.protobuf.util.Timestamps.toMillis;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.MetricsHintModel;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.background.PartitionBackgroundJob;
import io.littlehorse.server.streams.topology.core.background.PartitionJobContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Record;

/**
 * Replays the partition metric windows that were persisted to RocksDB but never aggregated, which
 * happens when a server dies (or a partition moves) between a metric being counted and its window
 * closing.
 *
 * <p>This used to run inline on the Kafka Streams thread as the "STORE" phase of
 * {@link PartitionDrainScheduler}, where it was the single most expensive thing a punctuation could
 * do: an unbounded RocksDB range scan that had to be chopped up with a 500ms budget and a resume
 * cursor to avoid blowing the transaction timeout. It now runs on the per-partition worker thread,
 * reading through IQv1 and emitting {@code PartitionAction}s for the punctuator to apply.
 *
 * <p><b>Why this is safe to move off-thread.</b> The scan covers a strictly historical key range:
 * everything from the persisted hint up to {@code serverStartWindowTime}, which is fixed at
 * construction to one millisecond before the window that was open when this partition was claimed.
 * Live processing only ever writes to the <i>current</i> window, which is always greater than that
 * bound. The background scan and the Streams thread therefore touch disjoint keys, so the stale
 * reads inherent to IQv1 cannot cause a lost update.
 *
 * <p>The job is idempotent and resumable: if it runs out of budget it persists its progress as a
 * {@link MetricsHintModel} and picks up from there on the next tick.
 */
@Slf4j
public class PartitionMetricsCatchUpJob implements PartitionBackgroundJob<CommandProcessorOutput> {

    private static final Duration INTERVAL = Duration.ofMillis(200);

    /**
     * How long a single scan pass may run before yielding. This no longer protects a Kafka
     * transaction — it bounds how long we hold an IQ iterator open, and how stale the actions we
     * enqueue can get.
     */
    private static final Duration SCAN_BUDGET = Duration.ofMillis(LHConstants.MAX_MS_PER_PARTITION_METRICS_PUNCTUATION);

    private final LHServerConfig config;
    private final long serverStartWindowTime;
    private final AtomicBoolean complete = new AtomicBoolean(false);
    private final BackgroundContext executionContext = new BackgroundContext();

    public PartitionMetricsCatchUpJob(LHServerConfig config) {
        this.config = config;
        this.serverStartWindowTime = LHUtil.getCurrentWindowDate().getTime() - 1;
    }

    /**
     * Read by the Kafka Streams thread to decide whether it may take over ownership of the metrics
     * hint. False until the historical range has been fully replayed.
     */
    public boolean isComplete() {
        return complete.get();
    }

    @Override
    public String name() {
        return "partition-metrics-catch-up";
    }

    @Override
    public Duration interval() {
        return INTERVAL;
    }

    @Override
    public void run(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException {
        if (complete.get()) {
            return;
        }

        ReadOnlyClusterScopedStore store = ctx.coreStore();
        MetricsHintModel hint = store.get(MetricsHintModel.METRICS_HINT_KEY, MetricsHintModel.class);
        if (hint == null || hint.getLastProcessedTimestamp() == null) {
            // No hint means nothing was ever persisted on this partition, so there is no history.
            markComplete();
            return;
        }

        long lastSeenWindowTime = toMillis(hint.getLastProcessedTimestamp());
        String startPrefix = LHConstants.PARTITION_METRICS_KEY + "/" + lastSeenWindowTime;
        String endPrefix = LHConstants.PARTITION_METRICS_KEY + "/" + serverStartWindowTime;
        Instant deadline = Instant.now().plus(SCAN_BUDGET);

        try (LHKeyValueIterator<PartitionMetricWindowModel> iter =
                store.range(startPrefix, endPrefix, PartitionMetricWindowModel.class)) {
            while (iter.hasNext()) {
                PartitionMetricWindowModel window = iter.next().getValue();
                if (window == null) {
                    continue;
                }
                scheduleAggregation(ctx, window);
                lastSeenWindowTime = window.getId().getWindowStart().getTime();

                if (Instant.now().isAfter(deadline)) {
                    log.debug(
                            "Metrics catch-up on partition {} yielding at {}, will resume next tick",
                            ctx.partition(),
                            new Date(lastSeenWindowTime));
                    ctx.put(null, new MetricsHintModel(fromMillis(lastSeenWindowTime)));
                    return;
                }
            }
        }

        ctx.put(null, new MetricsHintModel(fromMillis(lastSeenWindowTime)));
        markComplete();
    }

    private void markComplete() {
        if (complete.compareAndSet(false, true)) {
            log.info("Partition metrics catch-up complete; switching to in-memory collection");
        }
    }

    /**
     * Emits the two aggregate commands (spec-level and tenant-level) for a window, plus the delete
     * that retires it from the partition-local store.
     */
    private void scheduleAggregation(PartitionJobContext<CommandProcessorOutput> ctx, PartitionMetricWindowModel window)
            throws InterruptedException {
        // Each record gets its OWN copy of the window. The tenant-level aggregate is produced by
        // clearing the spec/task id on the window's id, and because actions are applied later on the
        // Streams thread rather than serialized eagerly by ctx.forward(), sharing one instance
        // between the two records would make both of them serialize as tenant-level.
        ctx.forward(aggregateRecord(copyOf(window), false));
        ctx.forward(aggregateRecord(copyOf(window), true));
        ctx.delete(null, window);
    }

    private PartitionMetricWindowModel copyOf(PartitionMetricWindowModel window) {
        return LHSerializable.fromProto(window.toProto().build(), PartitionMetricWindowModel.class, executionContext);
    }

    private Record<String, CommandProcessorOutput> aggregateRecord(
            PartitionMetricWindowModel window, boolean tenantLevel) {
        if (tenantLevel) {
            window.getId().markAsTenantMetricId();
        }
        TenantIdModel tenantId = window.getId().getTenantId();

        AggregateWindowMetricsModel aggregate = new AggregateWindowMetricsModel(window);
        LHTimer timer = new LHTimer(new CommandModel(aggregate, new Date()));
        timer.maturationTime = new Date();
        timer.topic = config.getCoreCmdTopicName();
        timer.setRepartition(true);
        timer.setTenantId(tenantId);

        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = aggregate.getPartitionKey();
        cpo.topic = config.getCoreCmdTopicName();
        cpo.payload = timer;

        return new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
    }
}
