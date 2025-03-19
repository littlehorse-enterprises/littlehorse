package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Sensor {

    private final Set<MetricSpecModel> metrics;
    private final ProcessorExecutionContext processorContext;
    private final GetableManager getableManager;

    public Sensor(final Set<MetricSpecIdModel> metricIds, final ProcessorExecutionContext processorContext) {
        this.processorContext = processorContext;
        metrics = metricIds.stream()
                .map(this::getMetricSpec)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        this.getableManager = processorContext.getableManager();
    }

    public void record(GetableStatusUpdate statusUpdate) {
        for (MetricSpecModel metricSpec : metrics) {
            for (AggregationType aggregationType : metricSpec.getAggregateAs()) {
                for (Duration windowLength : metricSpec.getWindowLengths()) {
                    PartitionMetricIdModel partitionId = new PartitionMetricIdModel(
                            metricSpec.getObjectId(), statusUpdate.getTenantId(), aggregationType);
                    PartitionMetricModel partitionMetric = Optional.ofNullable(getableManager.get(partitionId)).orElse(new PartitionMetricModel(
                                partitionId,
                                windowLength
                            ));
                    partitionMetric.incrementCurrentWindow(
                            LocalDateTime.now(), statusUpdate.getMetricIncrementValue(aggregationType));
                    processorContext.metricsInventory().addMetric(partitionMetric.getObjectId());
                }
            }
        }
    }

    private MetricSpecModel getMetricSpec(MetricSpecIdModel metricSpecId) {
        return processorContext.metadataManager().get(metricSpecId);
    }


}
