package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdateModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.RepartitionExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class RepartitionCommandProcessor implements Processor<String, RepartitionCommand, Void, Void> {

    private ProcessorContext<Void, Void> ctx;

    private KeyValueStore<String, Bytes> nativeStore;
    private final LHServerConfig lhConfig;
    private final MetadataCache metadataCache;

    public RepartitionCommandProcessor(LHServerConfig config, MetadataCache metadataCache) {
        this.lhConfig = config;
        this.metadataCache = metadataCache;
    }

    public void init(final ProcessorContext<Void, Void> ctx) {
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_REPARTITION_STORE);
        ctx.schedule(Duration.ofMinutes(1), PunctuationType.WALL_CLOCK_TIME, this::cleanOldMetrics);
    }

    @Override
    public void process(final Record<String, RepartitionCommand> record) {
        RepartitionExecutionContext repartitionContext = buildExecutionContext(record.headers());
        if (record.value() != null) {
            log.debug("Received a metric update!");
            TenantIdModel tenantId = HeadersUtil.tenantIdFromMetadata(record.headers());
            record.value().process(TenantScopedStore.newInstance(nativeStore, tenantId, repartitionContext), ctx);
        }
    }

    public void cleanOldMetrics(long timestamp) {
        final ClusterScopedStore store = ClusterScopedStore.newInstance(nativeStore, null);
        Date thirtyDaysAgo = DateUtils.addDays(new Date(), -30);
        cleanOldTaskMetrics(store, thirtyDaysAgo);
    }

    private void cleanOldTaskMetrics(ClusterScopedStore store, Date daysAgo) {
        try (LHKeyValueIterator<TaskMetricUpdateModel> iter =
                store.range("", LHUtil.toLhDbFormat(daysAgo), TaskMetricUpdateModel.class)) {
            while (iter.hasNext()) {
                log.trace("Skipping the cleaning of old metrics as they are currently not implemented.");

                /** LHIterKeyValue<TaskMetricUpdate> next = iter.next();
                 * TaskMetricUpdate metric = next.getValue();
                 * store.delete(metric.getStoreKey());
                 * String taskDefMetricKey = TaskDefMetricsModel.getObjectId(metric.type,
                 * metric.windowStart,
                 * metric.taskDefName);
                 * store.delete(taskDefMetricKey, TaskDefMetricsModel.class);*/
            }
        }
    }

    private RepartitionExecutionContext buildExecutionContext(Headers metadataHeaders) {
        return new RepartitionExecutionContext(metadataHeaders, lhConfig, ctx, metadataCache);
    }
}
