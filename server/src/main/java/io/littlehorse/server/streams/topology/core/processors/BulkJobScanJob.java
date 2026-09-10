package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardCursorModel;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardReportModel;
import io.littlehorse.common.model.getable.global.bulkjob.ActiveBulkJobModel;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.background.PartitionBackgroundJob;
import io.littlehorse.server.streams.topology.core.background.PartitionJobContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Record;

/**
 * Drives BulkJob shard scans for one partition. It performs two functions:
 *
 * <ol>
 *   <li><b>Discovery and claiming:</b> scans global metadata for RUNNING BulkJobs and creates a
 *       BulkJobShardCursor on this partition if not already claimed.</li>
 *   <li><b>Budgeted tag range scan:</b> for each claimed-but-incomplete shard, resumes scanning Tags
 *       to discover matching WfRunIds and emits one DeleteWfRunRequest command per match.</li>
 * </ol>
 *
 * <p>This was previously {@code BulkJobPunctuator}, running inline on the Kafka Streams thread with
 * a 50ms budget to avoid blowing the transaction timeout. It now runs on the per-partition worker,
 * reading through IQv1 and emitting {@code PartitionAction}s. Each deletion remains its own Kafka
 * Streams transaction, processed by the CommandProcessor as a normal command.
 *
 * <p><b>On stale reads.</b> Unlike the metrics replay, this scan is NOT disjoint from live
 * processing: the Streams thread is concurrently applying the very delete commands this job emits.
 * That was already true before the move — the job forwards deletes and only observes their effect
 * one or more transactions later — which is why the cursor logic in {@code BulkDeleteWfRunModel}
 * already backs off when it finds that the WfRun at the resume boundary still exists. IQv1 widens
 * that lag but does not change the shape of the problem, and the block-cache warming still works
 * because IQ reads the same local RocksDB instance the processor writes to.
 */
@Slf4j
public class BulkJobScanJob implements PartitionBackgroundJob<CommandProcessorOutput> {

    private static final Duration INTERVAL = Duration.ofSeconds(1);

    /**
     * Bounds how long a single pass may run. This no longer protects a Kafka transaction; it caps how
     * long the job holds IQ iterators open and how stale its enqueued actions can get before the
     * punctuator applies them.
     */
    private static final Duration DEFAULT_SCAN_BUDGET = Duration.ofMillis(500);

    private final LHServerConfig config;
    private final Duration scanBudget;

    /**
     * Maximum number of delete commands emitted per pass. With the scheduler's bounded queue this is
     * also what keeps a huge BulkJob from monopolising the action queue and starving other jobs.
     */
    private final long maxCommandsPerRun;

    /**
     * Source of "now" for the budget. Injectable so tests can drive the deadline deterministically.
     */
    private final Supplier<Instant> clock;

    public BulkJobScanJob(LHServerConfig config) {
        this(config, DEFAULT_SCAN_BUDGET, config.getMaxBulkJobCommandsPerTick(), Instant::now);
    }

    // For testing
    BulkJobScanJob(LHServerConfig config, Duration scanBudget, long maxCommandsPerRun, Supplier<Instant> clock) {
        this.config = config;
        this.scanBudget = scanBudget;
        this.maxCommandsPerRun = maxCommandsPerRun;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "bulk-job-scan";
    }

    @Override
    public Duration interval() {
        return INTERVAL;
    }

    @Override
    public void run(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException {
        final Instant deadline = clock.get().plus(scanBudget);
        final AtomicLong remainingCommandBudget = new AtomicLong(maxCommandsPerRun);
        final BooleanSupplier outOfBudget = () -> clock.get().isAfter(deadline) || remainingCommandBudget.get() == 0;

        for (ActiveBulkJobModel runningJob : findRunningJobs(ctx)) {
            if (outOfBudget.getAsBoolean()) {
                log.debug("Scan budget exhausted, will resume remaining jobs on next tick");
                return;
            }
            advanceShard(ctx, runningJob, outOfBudget, remainingCommandBudget);
        }
    }

    /**
     * Oldest first, so a long-running job cannot be starved by newer ones.
     */
    private List<ActiveBulkJobModel> findRunningJobs(PartitionJobContext<CommandProcessorOutput> ctx) {
        ReadOnlyClusterScopedStore clusterStore = ctx.metadataStore();
        String startKey = GetableClassEnum.ACTIVE_BULK_JOB.getNumber() + "/";
        String endKey = startKey + "~";

        List<ActiveBulkJobModel> runningJobs = new ArrayList<>();
        try (LHKeyValueIterator<?> range = clusterStore.range(startKey, endKey, StoredGetable.class)) {
            range.forEachRemaining(kv -> {
                StoredGetable<?, ?> value = (StoredGetable<?, ?>) kv.getValue();
                runningJobs.add((ActiveBulkJobModel) value.getStoredObject());
            });
        }
        runningJobs.sort(Comparator.comparing(ActiveBulkJobModel::getCreatedAt));
        return runningJobs;
    }

    private void advanceShard(
            PartitionJobContext<CommandProcessorOutput> ctx,
            ActiveBulkJobModel runningJob,
            BooleanSupplier outOfBudget,
            AtomicLong remainingCommandBudget)
            throws InterruptedException {
        BulkJobIdModel bulkJobId = runningJob.getId().getBulkJobId();
        TenantIdModel tenantId = runningJob.getId().getTenantId();
        ReadOnlyTenantScopedStore metadataStore = ctx.metadataStore(tenantId);
        ReadOnlyTenantScopedStore coreStore = ctx.coreStore(tenantId);

        StoredGetable<?, ?> storedJob = metadataStore.get(bulkJobId.getStoreableKey(), StoredGetable.class);
        if (storedJob == null) {
            // Metadata object not propagated yet; it will be picked up on a later tick.
            return;
        }
        BulkJobModel job = (BulkJobModel) storedJob.getStoredObject();
        if (job.getStatus() != BulkJobStatus.BULK_JOB_RUNNING) {
            return;
        }

        BulkJobShardCursorModel newCursor = new BulkJobShardCursorModel(bulkJobId);
        BulkJobShardCursorModel cursor = coreStore.get(newCursor.getStoreKey(), BulkJobShardCursorModel.class);
        cursor = cursor == null ? newCursor : cursor;
        if (cursor.isScanCompleted()) {
            return;
        }

        cursor = job.tryToComplete(
                record -> ctx.forward(uncheckedCast(record)),
                config,
                tenantId,
                coreStore,
                cursor,
                outOfBudget,
                remainingCommandBudget);

        ctx.forward(shardReport(ctx, job, cursor, tenantId));
        ctx.put(tenantId, cursor);
        // One shard advance is one atomic unit: the delete commands, the shard report and the
        // cursor must land together. A report claiming the shard is complete, applied without the
        // deletes that back it up, would retire WfRuns that were never touched.
        ctx.submit();
    }

    private Record<String, CommandProcessorOutput> shardReport(
            PartitionJobContext<CommandProcessorOutput> ctx,
            BulkJobModel job,
            BulkJobShardCursorModel cursor,
            TenantIdModel tenantId) {
        BulkJobShardReportModel report = new BulkJobShardReportModel(
                job.getId(),
                ctx.partition(),
                cursor.isScanCompleted(),
                cursor.getLastKey(),
                cursor.getLastSeenTimestamp());
        MetadataCommandModel command = new MetadataCommandModel(report);
        CommandProcessorOutput output =
                new CommandProcessorOutput(config.getMetadataCmdTopicName(), command, command.getPartitionKey());
        return new Record<>(
                output.partitionKey,
                output,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
    }

    /**
     * {@code BulkJobModel.tryToComplete} takes a raw {@code Consumer<Record>} for historical reasons;
     * every record it produces is a {@code CommandProcessorOutput}.
     */
    @SuppressWarnings("unchecked")
    private static Record<String, CommandProcessorOutput> uncheckedCast(Record<?, ?> record) {
        return (Record<String, CommandProcessorOutput>) record;
    }
}
