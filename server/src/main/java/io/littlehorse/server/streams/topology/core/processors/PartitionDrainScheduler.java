package io.littlehorse.server.streams.topology.core.processors;

import static com.google.protobuf.util.Timestamps.fromMillis;

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
import java.util.function.BooleanSupplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/**
 * Responsible for draining closed metric windows and counted tags from the in-memory
 * accumulators and forwarding them as repartition commands.
 *
 * <p>The historical metric-window replay that used to live here now runs off-thread in
 * {@link PartitionMetricsCatchUpJob}. What remains is work that is inherently bound to the Kafka
 * Streams thread, because it reads the {@link PartitionLocalBuffer}s that {@code process()} writes
 * to and which are explicitly not thread safe.
 */
@Slf4j
class PartitionDrainScheduler {

    private final PartitionLocalBuffer<PartitionMetricWindowModel> metricWindows;
    private final PartitionLocalBuffer<PartitionCountedTagModel> countedTags;
    private final LHServerConfig config;
    private final ProcessorContext<String, CommandProcessorOutput> ctx;

    /**
     * Whether {@link PartitionMetricsCatchUpJob} has finished replaying history. Until it has, the
     * job owns the metrics hint and we must not overwrite it, or we would claim that history has
     * been processed when it has not.
     */
    private final BooleanSupplier metricsCatchUpComplete;

    /**
     * The counted-tag replay is deliberately NOT a background job. Unlike metric windows, live
     * processing writes to the very same keys the replay scans, so doing it against a stale IQ
     * snapshot would delete counts that the Streams thread had incremented in the meantime.
     */
    private boolean countedTagsCaughtUp;

    PartitionDrainScheduler(
            PartitionLocalBuffer<PartitionMetricWindowModel> metricWindows,
            PartitionLocalBuffer<PartitionCountedTagModel> countedTags,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> ctx,
            BooleanSupplier metricsCatchUpComplete) {
        this.metricWindows = metricWindows;
        this.countedTags = countedTags;
        this.config = config;
        this.ctx = ctx;
        this.metricsCatchUpComplete = metricsCatchUpComplete;
    }

    /**
     * Called by the unified punctuator.
     */
    void punctuate(ClusterScopedStore store) {
        if (!countedTagsCaughtUp) {
            catchUpCountedTags(store);
            countedTagsCaughtUp = true;
        }
        flushFromMemory(store);
        if (metricsCatchUpComplete.getAsBoolean()) {
            long lastWindowTime = LHUtil.getCurrentWindowDate().getTime() - (2 * 60 * 1000L);
            store.put(new MetricsHintModel(fromMillis(lastWindowTime)));
        }
    }

    /**
     * Resets the flusher state (e.g., on partition close).
     */
    void reset() {
        this.countedTagsCaughtUp = false;
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
