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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Punctuator that runs on each CommandProcessor partition. It performs two functions:
 *
 * 1. Discovery & Claiming: Scans global metadata for RUNNING BulkJob objects and creates
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

    public BulkJobPunctuator(
            ProcessorContext<String, CommandProcessorOutput> ctx, LHServerConfig config, MetadataCache metadataCache) {
        this.ctx = ctx;
        this.config = config;
        this.metadataCache = metadataCache;
    }

    /**
     * Called periodically by the CommandProcessor punctuator schedule.
     */
    public void punctuate(long timestamp) {
        final int currentPartition = ctx.taskId().partition();
        final BackgroundContext context = new BackgroundContext();
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
        for (ActiveBulkJobModel runningJob : runningJobs) {
            BulkJobIdModel bulkJobId = runningJob.getId().getBulkJobId();
            TenantIdModel tenantId = runningJob.getId().getTenantId();
            TenantScopedStore metadataStore = TenantScopedStore.newInstance(metadataNativeStore, tenantId, context);
            TenantScopedStore coreStore = TenantScopedStore.newInstance(coreNativeStore, tenantId, context);
            StoredGetable<?, ?> bulkJob = metadataStore.get(bulkJobId.getStoreableKey(), StoredGetable.class);
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
            currentCursor = job.tryToComplete(ctx::forward, punctuateContext, currentCursor);
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
