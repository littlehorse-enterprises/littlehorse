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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
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
     * BulkJob is metadata, so every shard report is sent through the single metadata partition.
     * Keep the most recent progress in memory and publish it at a bounded rate.
     */
    private static final Duration PROGRESS_REPORT_INTERVAL = Duration.ofMinutes(1);

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

    /** Source of "now" for the scan budget and report cadence. Injectable for deterministic tests. */
    private final Supplier<Instant> clock;

    /**
     * Latest report for each tenant/job pair on this core partition. These are retained after a
     * timed send so {@link #flushPendingReports(Consumer)} can recover a report that was queued but
     * not committed before partition revocation.
     */
    private final Map<String, ShardProgress> shardProgress = new HashMap<>();

    /** Last time each shard's latest progress was queued for the metadata topology. */
    private final Map<String, Instant> lastReportAt = new HashMap<>();

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
                break;
            }
            advanceShard(ctx, runningJob, outOfBudget, remainingCommandBudget);
        }
        flushDueShardReports(ctx);
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
            discardProgress(tenantId, bulkJobId);
            return;
        }
        BulkJobModel job = (BulkJobModel) storedJob.getStoredObject();
        if (job.getStatus() != BulkJobStatus.BULK_JOB_RUNNING) {
            discardProgress(tenantId, bulkJobId);
            return;
        }

        BulkJobShardCursorModel newCursor = new BulkJobShardCursorModel(bulkJobId);
        BulkJobShardCursorModel cursor = coreStore.get(newCursor.getStoreKey(), BulkJobShardCursorModel.class);
        cursor = cursor == null ? newCursor : cursor;
        if (cursor.isScanCompleted()) {
            // A prior owner may have committed this cursor before its deferred metadata report.
            // Re-buffer it so the new owner can publish the completion immediately.
            recordShardProgress(ctx, job, cursor, tenantId);
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

        recordShardProgress(ctx, job, cursor, tenantId);
        ctx.put(tenantId, cursor);
        // One shard advance is one atomic unit: the delete commands and cursor must land together.
        // The metadata report is intentionally published separately and may lag by up to a minute.
        ctx.submit();
    }

    /**
     * Queues the latest report for each shard whose rate limit has elapsed. This runs on the worker
     * thread; the resulting actions are still forwarded only by the Kafka Streams thread.
     */
    private void flushDueShardReports(PartitionJobContext<CommandProcessorOutput> ctx) throws InterruptedException {
        Instant now = clock.get();
        List<String> dueReports = shardProgress.keySet().stream()
                .filter(key -> isReportDue(key, now))
                .toList();

        for (String key : dueReports) {
            ctx.forward(toRecord(shardProgress.get(key)));
        }
        if (!dueReports.isEmpty()) {
            ctx.submit();
            dueReports.forEach(key -> lastReportAt.put(key, now));
        }
    }

    /**
     * Called by {@link CommandProcessor#close()} after the worker has stopped. Reports sent through
     * the scheduler but not committed are discarded on revocation, so resend the latest state
     * directly from the closing Streams thread.
     */
    void flushPendingReports(Consumer<Record<String, CommandProcessorOutput>> commandOutput) {
        shardProgress.values().forEach(progress -> commandOutput.accept(toRecord(progress)));
        shardProgress.clear();
        lastReportAt.clear();
    }

    private boolean isReportDue(String key, Instant now) {
        Instant lastReport = lastReportAt.get(key);
        return lastReport == null || !now.isBefore(lastReport.plus(PROGRESS_REPORT_INTERVAL));
    }

    private void discardProgress(TenantIdModel tenantId, BulkJobIdModel bulkJobId) {
        String progressKey = progressKey(tenantId, bulkJobId);
        shardProgress.remove(progressKey);
        lastReportAt.remove(progressKey);
    }

    private String progressKey(TenantIdModel tenantId, BulkJobIdModel bulkJobId) {
        return tenantId.getId() + "/" + bulkJobId.getId();
    }

    private void recordShardProgress(
            PartitionJobContext<CommandProcessorOutput> ctx,
            BulkJobModel job,
            BulkJobShardCursorModel cursor,
            TenantIdModel tenantId) {
        shardProgress.put(
                progressKey(tenantId, job.getId()), new ShardProgress(tenantId, shardReport(ctx, job, cursor)));
    }

    private BulkJobShardReportModel shardReport(
            PartitionJobContext<CommandProcessorOutput> ctx, BulkJobModel job, BulkJobShardCursorModel cursor) {
        BulkJobShardReportModel report = new BulkJobShardReportModel(
                job.getId(),
                ctx.partition(),
                cursor.isScanCompleted(),
                cursor.getLastKey(),
                cursor.getLastSeenTimestamp());
        return report;
    }

    private Record<String, CommandProcessorOutput> toRecord(ShardProgress progress) {
        BulkJobShardReportModel report = progress.report();
        MetadataCommandModel command = new MetadataCommandModel(report);
        CommandProcessorOutput output =
                new CommandProcessorOutput(config.getMetadataCmdTopicName(), command, command.getPartitionKey());
        return new Record<>(
                output.partitionKey,
                output,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(
                        progress.tenantId(), new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
    }

    private record ShardProgress(TenantIdModel tenantId, BulkJobShardReportModel report) {}

    /**
     * {@code BulkJobModel.tryToComplete} takes a raw {@code Consumer<Record>} for historical reasons;
     * every record it produces is a {@code CommandProcessorOutput}.
     */
    @SuppressWarnings("unchecked")
    private static Record<String, CommandProcessorOutput> uncheckedCast(Record<?, ?> record) {
        return (Record<String, CommandProcessorOutput>) record;
    }
}
