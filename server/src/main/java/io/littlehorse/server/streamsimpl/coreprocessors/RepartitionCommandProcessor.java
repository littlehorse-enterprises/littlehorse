package io.littlehorse.server.streamsimpl.coreprocessors;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.metrics.TaskDefMetrics;
import io.littlehorse.common.model.metrics.WfSpecMetrics;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHKeyValueIterator;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

@Slf4j
public class RepartitionCommandProcessor
    implements Processor<String, RepartitionCommand, Void, Void> {

    private LHStoreWrapper store;
    private LHConfig config;
    private ProcessorContext<Void, Void> ctx;

    public RepartitionCommandProcessor(LHConfig config) {
        this.config = config;
    }

    public void init(final ProcessorContext<Void, Void> ctx) {
        this.ctx = ctx;
        store =
            new LHStoreWrapper(
                ctx.getStateStore(ServerTopology.CORE_REPARTITION_STORE),
                config
            );

        ctx.schedule(
            Duration.ofMinutes(1),
            PunctuationType.WALL_CLOCK_TIME,
            this::cleanOldMetrics
        );
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
        try (
            LHKeyValueIterator<TaskMetricUpdate> iter = store.range(
                "",
                LHUtil.toLhDbFormat(daysAgo),
                TaskMetricUpdate.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskMetricUpdate> next = iter.next();
                TaskMetricUpdate metric = next.getValue();
                store.delete(metric.getStoreKey());
                String taskDefMetricKey = TaskDefMetrics.getObjectId(
                    metric.type,
                    metric.windowStart,
                    metric.taskDefName
                );
                store.delete(taskDefMetricKey, TaskDefMetrics.class);
            }
        }
    }

    private void cleanOldWfMetrics(Date daysAgo) {
        try (
            LHKeyValueIterator<WfMetricUpdate> iter = store.range(
                "",
                LHUtil.toLhDbFormat(daysAgo),
                WfMetricUpdate.class
            )
        ) {
            while (iter.hasNext()) {
                LHIterKeyValue<WfMetricUpdate> next = iter.next();
                WfMetricUpdate metric = next.getValue();
                store.delete(metric.getStoreKey());
                String wfSpecMetricKey = WfSpecMetrics.getObjectId(
                    metric.type,
                    metric.windowStart,
                    metric.wfSpecName,
                    metric.wfSpecVersion
                );
                store.delete(wfSpecMetricKey, WfSpecMetrics.class);
            }
        }
    }
}
