package io.littlehorse.server.streams.topology.core.processors;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdateModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.WfMetricUpdateModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

@Slf4j
public class RepartitionCommandProcessor implements Processor<String, RepartitionCommand, Void, Void> {

    private RocksDBWrapper store;
    private LHServerConfig config;
    private ProcessorContext<Void, Void> ctx;

    public RepartitionCommandProcessor(LHServerConfig config) {
        this.config = config;
    }

    public void init(final ProcessorContext<Void, Void> ctx) {
        this.ctx = ctx;
        store = new RocksDBWrapper(ctx.getStateStore(ServerTopology.CORE_REPARTITION_STORE), config);

        ctx.schedule(Duration.ofMinutes(1), PunctuationType.WALL_CLOCK_TIME, this::cleanOldMetrics);
    }

    public void process(final Record<String, RepartitionCommand> record) {
        if (record.value() != null) {
            log.debug("Received a metric update!");
            record.value().process(store, ctx);
        }
    }

    public void cleanOldMetrics(long timestamp) {
        Date thirtyDaysAgo = DateUtils.addDays(new Date(), -30);
        cleanOldTaskMetrics(thirtyDaysAgo);
        cleanOldWfMetrics(thirtyDaysAgo);
    }

    private void cleanOldTaskMetrics(Date daysAgo) {
        try (LHKeyValueIterator<TaskMetricUpdateModel> iter =
                store.range("", LHUtil.toLhDbFormat(daysAgo), TaskMetricUpdateModel.class)) {
            while (iter.hasNext()) {
                log.trace("Skipping the cleaning of old metrics as they are currently not implemented.");
                /*
                 * LHIterKeyValue<TaskMetricUpdate> next = iter.next();
                 * TaskMetricUpdate metric = next.getValue();
                 * store.delete(metric.getStoreKey());
                 * String taskDefMetricKey = TaskDefMetricsModel.getObjectId(metric.type,
                 * metric.windowStart,
                 * metric.taskDefName);
                 * store.delete(taskDefMetricKey, TaskDefMetricsModel.class);
                 */
            }
        }
    }

    private void cleanOldWfMetrics(Date daysAgo) {
        try (LHKeyValueIterator<WfMetricUpdateModel> iter =
                store.range("", LHUtil.toLhDbFormat(daysAgo), WfMetricUpdateModel.class)) {
            while (iter.hasNext()) {
                log.trace("Skipping the cleaning of old metrics as they are currently not implemented.");
                /*
                 * LHIterKeyValue<WfMetricUpdate> next = iter.next();
                 * WfMetricUpdate metric = next.getValue();
                 * store.delete(metric.getStoreKey());
                 * String wfSpecMetricKey = WfSpecMetricsModel.getObjectId(
                 * metric.type, metric.windowStart, metric.wfSpecName, metric.wfSpecVersion);
                 * store.delete(wfSpecMetricKey, WfSpecMetricsModel.class);
                 */
            }
        }
    }
}
