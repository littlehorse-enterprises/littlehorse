package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.LHStore;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class RepartitionCommandProcessor implements Processor<String, RepartitionCommand, Void, Void> {

    private LHServerConfig config;
    private ProcessorContext<Void, Void> ctx;

    private KeyValueStore<String, Bytes> nativeStore;

    public RepartitionCommandProcessor(LHServerConfig config) {
        this.config = config;
    }

    public void init(final ProcessorContext<Void, Void> ctx) {
        this.ctx = ctx;
        this.nativeStore = ctx.getStateStore(ServerTopology.CORE_REPARTITION_STORE);
        ctx.schedule(Duration.ofMinutes(1), PunctuationType.WALL_CLOCK_TIME, this::cleanOldMetrics);
    }

    public void process(final Record<String, RepartitionCommand> record) {
        if (record.value() != null) {
            log.debug("Received a metric update!");
            RepartitionCommand command = record.value();
            record.value().process(LHStore.instanceFor(nativeStore, command.getTenantId()), ctx);
        }
    }

    public void cleanOldMetrics(long timestamp) {
        final LHStore defaultStore = LHStore.defaultStore(nativeStore);
        Date thirtyDaysAgo = DateUtils.addDays(new Date(), -30);
        cleanOldTaskMetrics(defaultStore, thirtyDaysAgo);
        cleanOldWfMetrics(defaultStore, thirtyDaysAgo);
    }

    private void cleanOldTaskMetrics(LHStore defaultStore, Date daysAgo) {
        try (LHKeyValueIterator<TaskMetricUpdate> iter =
                defaultStore.range("", LHUtil.toLhDbFormat(daysAgo), TaskMetricUpdate.class)) {
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

    private void cleanOldWfMetrics(LHStore defaultStore, Date daysAgo) {
        try (LHKeyValueIterator<WfMetricUpdate> iter =
                defaultStore.range("", LHUtil.toLhDbFormat(daysAgo), WfMetricUpdate.class)) {
            while (iter.hasNext()) {
                log.trace("Skipping the cleaning of old metrics as they are currently not implemented.");

                /* * LHIterKeyValue<WfMetricUpdate> next = iter.next();
                 * WfMetricUpdate metric = next.getValue();
                 * store.delete(metric.getStoreKey());
                 * String wfSpecMetricKey = WfSpecMetricsModel.getObjectId(
                 * metric.type, metric.windowStart, metric.wfSpecName, metric.wfSpecVersion);
                 * store.delete(wfSpecMetricKey, WfSpecMetricsModel.class);*/

            }
        }
    }
}
