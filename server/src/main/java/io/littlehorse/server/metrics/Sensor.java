package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sensor {

    private final Set<MetricSpecModel> metrics;
    private final ProcessorExecutionContext processorContext;
    private final GetableManager getableManager;
    private final TenantIdModel tenantId;

    public Sensor(final Set<MetricSpecIdModel> metricIds, final ProcessorExecutionContext processorContext) {
        this.processorContext = processorContext;
        metrics = metricIds.stream()
                .map(this::getMetricSpec)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        this.getableManager = processorContext.getableManager();
        this.tenantId = processorContext.authorization().tenantId();
    }

    public void record(GetableStatusUpdate statusUpdate) {
        for (MetricSpecModel metricSpec : metrics) {
            for (AggregationType aggregationType : metricSpec.getAggregateAs()) {
                for (Duration windowLength : metricSpec.getWindowLengths()) {
                    PartitionMetricIdModel partitionId =
                            new PartitionMetricIdModel(metricSpec.getObjectId(), tenantId, aggregationType);
                    PartitionMetricModel partitionMetric = Optional.ofNullable(getableManager.get(partitionId))
                            .orElse(new PartitionMetricModel(partitionId, windowLength));
                    partitionMetric.incrementCurrentWindow(
                            LocalDateTime.now(), statusUpdate.getMetricIncrementValue(aggregationType));
                    processorContext.getableManager().put(partitionMetric);
                    processorContext.metricsInventory().addMetric(partitionMetric.getObjectId());
                }
            }
        }
    }

    private MetricSpecModel getMetricSpec(MetricSpecIdModel metricSpecId) {
        return Optional.ofNullable(processorContext.metadataManager().get(metricSpecId))
                .orElseGet(() -> {
                    log.info(metricSpecId + " not found");
                    return null;
                });
    }
}
