package io.littlehorse.server.streams.topology.core.processors;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static com.google.protobuf.util.Timestamps.toMillis;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.UpdateCountedTagModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.metadatacommand.subcommand.AggregateWindowMetricsModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.storeinternals.MetricsHintModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionLocalBuffer;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/**
 * Responsible for draining closed metric windows and counted tags from the in-memory
 * accumulators and forwarding them as repartition commands. Also handles the store-based
 * catch-up scan after startup.
 */
@Slf4j
class PartitionDrainScheduler {

    private final PartitionLocalBuffer<PartitionMetricWindowModel> metricWindows;
    private final PartitionLocalBuffer<PartitionCountedTagModel> countedTags;
    private final LHServerConfig config;
    private final ProcessorContext<String, CommandProcessorOutput> ctx;

    private MetricsCollectionSource collectionSource;
    private final long serverStartWindowTime;

    PartitionDrainScheduler(
            PartitionLocalBuffer<PartitionMetricWindowModel> metricWindows,
            PartitionLocalBuffer<PartitionCountedTagModel> countedTags,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> ctx) {
        this.metricWindows = metricWindows;
        this.countedTags = countedTags;
        this.config = config;
        this.ctx = ctx;
        this.collectionSource = MetricsCollectionSource.STORE;
        this.serverStartWindowTime = LHUtil.getCurrentWindowDate().getTime() - 1;
    }

    /**
     * Called by the punctuator. Decides whether to catch up from RocksDB or drain memory,
     * then persists the hint.
     */
    void punctuate(ClusterScopedStore store) {
        long lastWindowTime = LHUtil.getCurrentWindowDate().getTime() - (2 * 60 * 1000L);
        if (collectionSource == MetricsCollectionSource.STORE) {
            lastWindowTime = catchUpFromStore(store);
        } else {
            flushFromMemory(store);
        }
        store.put(new MetricsHintModel(fromMillis(lastWindowTime)));
    }

    /**
     * Resets the flusher state (e.g., on partition close).
     */
    void reset() {
        this.collectionSource = MetricsCollectionSource.STORE;
    }

    private void flushFromMemory(ClusterScopedStore store) {
        flushClosedWindows(store);
        flushCountedTags(store);
    }

    private void flushClosedWindows(ClusterScopedStore store) {
        Date currentWindow = LHUtil.getCurrentWindowDate();
        long startTime = System.currentTimeMillis();

        List<PartitionMetricWindowModel> closed = metricWindows.drain(metric -> {
            if (System.currentTimeMillis() - startTime > LHConstants.MAX_MS_PER_PARTITION_METRICS_PUNCTUATION) {
                return false;
            }
            return metric.getId().getWindowStart().before(currentWindow);
        });

        for (PartitionMetricWindowModel metric : closed) {
            forwardMetricWindow(store, metric);
        }
    }

    private void flushCountedTags(ClusterScopedStore store) {
        List<PartitionCountedTagModel> tags = countedTags.drainAll();
        for (PartitionCountedTagModel tag : tags) {
            forwardCountedTag(tag);
            store.delete(tag);
        }
    }

    private long catchUpFromStore(ClusterScopedStore store) {
        collectionSource = MetricsCollectionSource.MEMORY;

        // Catch up counted tags first (forward and delete all persisted deltas)
        catchUpCountedTags(store);

        // Then catch up metric windows (resumable with hint)
        MetricsHintModel hint = store.get(MetricsHintModel.METRICS_HINT_KEY, MetricsHintModel.class);
        if (hint == null || hint.getLastProcessedTimestamp() == null) {
            return this.serverStartWindowTime;
        }

        long lastSeenWindowTime = toMillis(hint.getLastProcessedTimestamp());
        String startPrefix = LHConstants.PARTITION_METRICS_KEY + "/" + lastSeenWindowTime;
        String endPrefix = LHConstants.PARTITION_METRICS_KEY + "/" + serverStartWindowTime;
        long startTime = System.currentTimeMillis();

        try (LHKeyValueIterator<PartitionMetricWindowModel> iter =
                store.range(startPrefix, endPrefix, PartitionMetricWindowModel.class)) {
            while (iter.hasNext()) {
                PartitionMetricWindowModel windowMetrics = iter.next().getValue();
                if (windowMetrics != null) {
                    lastSeenWindowTime = forwardMetricWindow(store, windowMetrics);
                    if (System.currentTimeMillis() - startTime > LHConstants.MAX_MS_PER_PARTITION_METRICS_PUNCTUATION) {
                        collectionSource = MetricsCollectionSource.STORE;
                        log.warn(
                                "Hint will be used for next punctuation from: {} to: {} elapsedTime: {} ms",
                                new Date(lastSeenWindowTime),
                                new Date(serverStartWindowTime),
                                System.currentTimeMillis() - startTime);
                        return lastSeenWindowTime;
                    }
                }
            }
        }
        return lastSeenWindowTime;
    }

    private void catchUpCountedTags(ClusterScopedStore store) {
        String prefix = Storeable.getSubstorePrefix(StoreableType.PARTITION_COUNTED_TAG);
        try (LHKeyValueIterator<PartitionCountedTagModel> iter =
                store.prefixScan(prefix, PartitionCountedTagModel.class)) {
            while (iter.hasNext()) {
                PartitionCountedTagModel tag = iter.next().getValue();
                if (tag != null) {
                    forwardCountedTag(tag);
                    store.delete(tag);
                }
            }
        }
    }

    private long forwardMetricWindow(ClusterScopedStore store, PartitionMetricWindowModel metric) {
        AggregateWindowMetricsModel aggregate = new AggregateWindowMetricsModel(metric);
        forwardAggregateCommand(aggregate);
        store.delete(metric);
        // Also forward tenant-level aggregate
        metric.getId().markAsTenantMetricId();
        forwardAggregateCommand(aggregate);
        return metric.getId().getWindowStart().getTime();
    }

    private void forwardAggregateCommand(AggregateWindowMetricsModel aggregate) {
        TenantIdModel tenantId = aggregate.getMetricWindow().getId().getTenantId();
        CommandModel command = new CommandModel(aggregate, new Date());
        LHTimer timer = new LHTimer(command);
        timer.maturationTime = new Date();
        timer.topic = config.getCoreCmdTopicName();
        timer.setRepartition(true);
        timer.setTenantId(tenantId);

        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = aggregate.getPartitionKey();
        cpo.topic = config.getCoreCmdTopicName();
        cpo.payload = timer;

        ctx.forward(new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(tenantId, new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL))));
    }

    private void forwardCountedTag(PartitionCountedTagModel tag) {
        UpdateCountedTagModel updateCountedTag = new UpdateCountedTagModel(tag.getAttributeString(), tag.getCount());
        CommandModel command = new CommandModel(updateCountedTag);
        LHTimer timer = new LHTimer(command, true);
        timer.topic = config.getCoreCmdTopicName();
        timer.setTenantId(tag.getTenantId());

        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = timer.getPartitionKey();
        cpo.topic = config.getCoreCmdTopicName();
        cpo.payload = timer;

        ctx.forward(new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(
                        tag.getTenantId(), new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL))));
    }
}
