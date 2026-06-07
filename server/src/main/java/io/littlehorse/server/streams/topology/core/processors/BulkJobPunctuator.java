package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.InternalDeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.job.BulkJobShardCursorModel;
import io.littlehorse.common.model.getable.global.bulkjob.BulkJobModel;
import io.littlehorse.common.model.getable.objectId.BulkJobIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.BulkJob;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.BackgroundContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
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

    public BulkJobPunctuator(
            ProcessorContext<String, CommandProcessorOutput> ctx,
            LHServerConfig config) {
        this.ctx = ctx;
        this.config = config;
    }

    /**
     * Called periodically by the CommandProcessor punctuator schedule.
     */
    public void punctuate(long timestamp) {
        KeyValueStore<String, Bytes> globalStore = ctx.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
        KeyValueStore<String, Bytes> coreStore = ctx.getStateStore(ServerTopology.CORE_STORE);
        ClusterScopedStore clusterStore = ClusterScopedStore.newInstance(globalStore, new BackgroundContext());

        // Scan for all BulkJob metadata objects (stored getables)
        String prefix = StoreableType.STORED_GETABLE.getNumber() + "/"
                + GetableClassEnum.BULK_JOB.getNumber() + "/";
        try (LHKeyValueIterator<?> bulkJobs = clusterStore.range(prefix, prefix + "~", StoredGetable.class)) {
            while (bulkJobs.hasNext()) {
                LHIterKeyValue<?> entry = bulkJobs.next();
                @SuppressWarnings("unchecked")
                StoredGetable<BulkJob, BulkJobModel> storedGetable =
                        (StoredGetable<BulkJob, BulkJobModel>) entry.getValue();
                BulkJobModel bulkJob = storedGetable.getStoredObject();

                if (bulkJob.getStatus() != BulkJobStatus.BULK_JOB_RUNNING) {
                    continue;
                }

                BulkJobIdModel jobId = bulkJob.getObjectId();
                String cursorFullKey = getCursorFullStoreKey(jobId);

                Bytes existingCursorBytes = coreStore.get(cursorFullKey);
                BulkJobShardCursorModel cursor;

                if (existingCursorBytes == null) {
                    // Create a new cursor (claim) for this partition
                    cursor = new BulkJobShardCursorModel(jobId, ctx.taskId().partition());
                    coreStore.put(cursorFullKey, Bytes.wrap(cursor.toBytes()));
                    log.info("BulkJob {} claimed on partition {}.", jobId, ctx.taskId().partition());
                } else {
                    cursor = LHSerializable.fromBytes(
                            existingCursorBytes.get(), BulkJobShardCursorModel.class, new BackgroundContext());
                }

                // If scan already completed for this shard, skip
                if (cursor.isScanCompleted()) {
                    continue;
                }

                // Perform time-budgeted Tag range scan
                performTagRangeScan(bulkJob, cursor, coreStore);
            }
        }
    }

    private void performTagRangeScan(
            BulkJobModel bulkJob, BulkJobShardCursorModel cursor, KeyValueStore<String, Bytes> coreStore) {

        BulkDeleteWfRun criteria = bulkJob.getBulkDeleteWfRun();
        String attributeString = buildTagAttributeString(criteria);

        // Build the full Tag store key range using the time boundaries.
        // Tag store key format: {StoreableType.TAG}/{objectType}/__key_val/__key_val/{createdAt}/{objectId}
        String tagPrefix = StoreableType.TAG.getNumber() + "/";
        String earliestTs = LHUtil.toLhDbFormat(LHUtil.fromProtoTs(criteria.getEarliestStart()));
        String latestTs = LHUtil.toLhDbFormat(LHUtil.fromProtoTs(criteria.getLatestStart()));

        String startKey = tagPrefix + attributeString + "/" + earliestTs;
        String endKey = tagPrefix + attributeString + "/" + latestTs + "/~";

        // If we have a cursor from a previous iteration, resume from there
        String resumeKey = cursor.getLastKey();
        if (resumeKey != null && !resumeKey.isEmpty()) {
            startKey = resumeKey;
        }

        Date limitTime = DateUtils.addMilliseconds(new Date(), config.getMaxBulkJobIterDurationMs());
        int forwarded = 0;

        try (var iter = coreStore.range(Bytes.wrap(startKey.getBytes()), Bytes.wrap(endKey.getBytes()))) {
            while (iter.hasNext() && new Date().before(limitTime)) {
                var kv = iter.next();
                String tagKey = new String(kv.key.get());

                // Parse the Tag to get the described object ID (WfRunId)
                Tag tag = LHSerializable.fromBytes(kv.value.get(), Tag.class, new BackgroundContext());
                String wfRunId = tag.getDescribedObjectId();

                // Forward a DeleteWfRunRequest command keyed by the WfRunId
                forwardDeleteCommand(wfRunId);
                forwarded++;

                // Update cursor's last key for resume
                cursor.setLastKey(tagKey);
            }

            if (!iter.hasNext()) {
                // Scan completed for this shard
                cursor.setScanCompleted(true);
                log.info("BulkJob {} scan completed on partition {}. Forwarded {} deletions.",
                        cursor.getBulkJobId(), ctx.taskId().partition(), forwarded);
            } else {
                log.debug("BulkJob {} scan paused on partition {} (time budget). Forwarded {} this tick.",
                        cursor.getBulkJobId(), ctx.taskId().partition(), forwarded);
            }
        }

        // Persist updated cursor
        String cursorFullKey = getCursorFullStoreKey(cursor.getBulkJobId());
        coreStore.put(cursorFullKey, Bytes.wrap(cursor.toBytes()));
    }

    private String buildTagAttributeString(BulkDeleteWfRun criteria) {
        // Build attribute string matching WfRun Tag format.
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("wfSpecName", criteria.getWfSpecName()));
        if (criteria.hasStatus()) {
            attributes.add(new Attribute("status", criteria.getStatus().toString()));
        }
        return Tag.getAttributeString(GetableClassEnum.WF_RUN, attributes);
    }

    private void forwardDeleteCommand(String wfRunId) {
        InternalDeleteWfRunRequestModel deleteRequest = new InternalDeleteWfRunRequestModel();
        deleteRequest.setWfRunId(new WfRunIdModel(wfRunId));

        CommandModel command = new CommandModel(deleteRequest);

        LHTimer timer = new LHTimer(command, true);
        timer.topic = config.getCoreCmdTopicName();

        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = wfRunId;
        cpo.topic = config.getCoreCmdTopicName();
        cpo.payload = timer;

        TenantIdModel tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);
        PrincipalIdModel principalId = new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL);

        ctx.forward(new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, principalId)));
    }

    private String getCursorFullStoreKey(BulkJobIdModel jobId) {
        BulkJobShardCursorModel tempCursor = new BulkJobShardCursorModel(jobId, ctx.taskId().partition());
        return tempCursor.getFullStoreKey();
    }
}
