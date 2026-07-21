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
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.PunctuationExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Punctuator that runs on each CommandProcessor partition. It performs two functions:
 *
 * 1. Discovery and Claiming: Scans global metadata for RUNNING BulkJob objects and creates
 *    a BulkJobShardCursor on this partition if not already claimed.
 *
 * 2. Time-budgeted Tag Range Scan: For each claimed-but-incomplete shard, resumes scanning
 *    Tags to discover matching WfRunIds and forwards individual DeleteWfRunRequest commands
 *    via the core command topic.
 *
 * Each deletion is its own Kafka Streams transaction (processed by the CommandProcessor as
 * a normal command), ensuring the processor is never blocked by large batch operations.
 */
@Slf4j
public class BulkJobPunctuator {

    private final ProcessorContext<String, CommandProcessorOutput> ctx;
    private final LHServerConfig config;
    private final MetadataCache metadataCache;

    /**
     * Maximum time the punctuator is allowed to run before yielding back to Kafka Streams.
     * This prevents transaction timeouts when there are many active BulkJobs or large scans.
     */
    private final Duration punctuationBudget;

    /**
     * Maximum number of commands the punctuator is allowed to forward in a single tick before
     * yielding back to Kafka Streams. This bounds how many records a single punctuation appends
     * within one transaction, complementing the wall-clock {@link #punctuationBudget}. Like the
     * time budget, it is enforced through the shared {@code outOfBudget} predicate at both the
     * inter-job and intra-job levels.
     */
    private final long maxCommandsPerPunctuation;

    /**
     * Source of "now" used to evaluate the punctuation budget. Injectable so tests can drive the
     * deadline deterministically; production uses {@link Instant#now()}.
     */
    private final Supplier<Instant> clock;

    public BulkJobPunctuator(
            ProcessorContext<String, CommandProcessorOutput> ctx,
            LHServerConfig config,
            MetadataCache metadataCache,
            Duration punctuationBudget,
            long maxCommandsPerPunctuation) {
        this(ctx, config, metadataCache, punctuationBudget, maxCommandsPerPunctuation, Instant::now);
    }

    // For testing
    BulkJobPunctuator(
            ProcessorContext<String, CommandProcessorOutput> ctx,
            LHServerConfig config,
            MetadataCache metadataCache,
            Duration punctuationBudget,
            long maxCommandsPerPunctuation,
            Supplier<Instant> clock) {
        this.ctx = ctx;
        this.config = config;
        this.metadataCache = metadataCache;
        this.punctuationBudget = punctuationBudget;
        this.maxCommandsPerPunctuation = maxCommandsPerPunctuation;
        this.clock = clock;
    }

    /**
     * Called periodically by the CommandProcessor punctuator schedule.
     */
    public void punctuate(long timestamp) {
        final Instant deadline = clock.get().plus(punctuationBudget);
        final AtomicLong remainingCommandBudget = new AtomicLong(maxCommandsPerPunctuation);
        final BooleanSupplier outOfBudget = () -> clock.get().isAfter(deadline) || remainingCommandBudget.get() == 0;
        final int currentPartition = ctx.taskId().partition();
        final BackgroundContext context = new BackgroundContext(config);
        KeyValueStore<String, Bytes> metadataNativeStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        KeyValueStore<String, Bytes> coreNativeStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(metadataNativeStore, context);

        String startKey = GetableClassEnum.ACTIVE_BULK_JOB.getNumber() + "/";
        String endKey = startKey + "~";
        List<ActiveBulkJobModel> runningJobs = new ArrayList<>();
        try (LHKeyValueIterator<?> range = clusterStore.range(startKey, endKey, StoredGetable.class)) {
            range.forEachRemaining(kv -> {
                StoredGetable<?, ?> value = (StoredGetable<?, ?>) kv.getValue();
                ActiveBulkJobModel activeBulkJob = (ActiveBulkJobModel) value.getStoredObject();
                runningJobs.add(activeBulkJob);
            });
        }
        // Oldest first
        runningJobs.sort(Comparator.comparing(ActiveBulkJobModel::getCreatedAt));
        for (ActiveBulkJobModel runningJob : runningJobs) {
            // Check deadline before processing each job
            if (outOfBudget.getAsBoolean()) {
                log.debug("Punctuation budget exhausted, will resume remaining jobs on next tick");
                break;
            }
            BulkJobIdModel bulkJobId = runningJob.getId().getBulkJobId();
            TenantIdModel tenantId = runningJob.getId().getTenantId();
            TenantScopedStore metadataStore = TenantScopedStore.newInstance(metadataNativeStore, tenantId, context);
            TenantScopedStore coreStore = TenantScopedStore.newInstance(coreNativeStore, tenantId, context);
            StoredGetable<?, ?> bulkJob = metadataStore.get(bulkJobId.getStoreableKey(), StoredGetable.class);
            if (bulkJob == null) {
                // Metadata object not propagated yet, skip and will be picked up on next tick
                continue;
            }
            BulkJobModel job = (BulkJobModel) bulkJob.getStoredObject();
            if (job.getStatus() != BulkJobStatus.BULK_JOB_RUNNING) {
                continue;
            }
            BulkJobShardCursorModel newCursor = new BulkJobShardCursorModel(bulkJobId);
            BulkJobShardCursorModel currentCursor =
                    coreStore.get(newCursor.getStoreKey(), BulkJobShardCursorModel.class);
            currentCursor = currentCursor == null ? newCursor : currentCursor;
            // If scan already completed for this shard, skip
            if (currentCursor.isScanCompleted()) {
                continue;
            }
            PunctuationExecutionContext punctuateContext = new PunctuationExecutionContext(
                    timestamp, config, metadataNativeStore, coreNativeStore, metadataCache, tenantId);
            currentCursor = job.tryToComplete(
                    ctx::forward, punctuateContext, currentCursor, outOfBudget, remainingCommandBudget);
            CommandProcessorOutput processorOutput = createBulkJobReport(job, currentPartition, currentCursor);
            Record<String, CommandProcessorOutput> reportRecord = new Record<>(
                    processorOutput.partitionKey,
                    processorOutput,
                    timestamp,
                    HeadersUtil.metadataHeadersFor(tenantId, new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)));
            ctx.forward(reportRecord);
            coreStore.put(currentCursor);
        }
    }

    private CommandProcessorOutput createBulkJobReport(
            BulkJobModel job, int currentPartition, BulkJobShardCursorModel currentCursor) {
        BulkJobShardReportModel report = new BulkJobShardReportModel(
                job.getId(),
                currentPartition,
                currentCursor.isScanCompleted(),
                currentCursor.getLastKey(),
                currentCursor.getLastSeenTimestamp());
        MetadataCommandModel command = new MetadataCommandModel(report);
        return new CommandProcessorOutput(config.getMetadataCmdTopicName(), command, command.getPartitionKey());
    }
}
