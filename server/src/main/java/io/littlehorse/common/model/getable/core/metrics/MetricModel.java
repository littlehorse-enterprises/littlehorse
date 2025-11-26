package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.RepartitionWindowedMetricModel;
import io.littlehorse.common.model.RepartitionedGetable;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricModel extends RepartitionedGetable<Metric> {

    private MetricIdModel metricRunId;
    private Date createdAt;
    private Long count;
    private Duration latencyAvg;
    private Map<Integer, Double> valuePerPartition = new HashMap<>();

    public MetricModel() {}

    public MetricModel(MetricIdModel metricRunId) {
        this.metricRunId = metricRunId;
        this.createdAt = new Date();
        log.info("creating new metric" + metricRunId);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        Metric p = (Metric) proto;
        this.metricRunId = LHSerializable.fromProto(p.getId(), MetricIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        switch (p.getValueCase()) {
            case COUNT -> this.count = p.getCount();
            case LATENCY_AVG -> this.latencyAvg = Duration.ofMillis(p.getLatencyAvg());
            default -> throw new IllegalStateException("Unexpected value: " + p.getValueCase());
        }
        this.valuePerPartition = new HashMap<>(p.getValuePerPartitionMap());
    }

    @Override
    public Metric.Builder toProto() {
        Metric.Builder out = Metric.newBuilder()
                .setId(metricRunId.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .putAllValuePerPartition(valuePerPartition);
        if (count != null) {
            out.setCount(count);
        } else if (latencyAvg != null) {
            out.setLatencyAvg(latencyAvg.toMillis());
        } else {
            throw new IllegalStateException("Unexpected metric value");
        }
        return out;
    }

    @Override
    public Class<Metric> getProtoBaseClass() {
        return Metric.class;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of();
    }

    @Override
    public MetricIdModel getObjectId() {
        return metricRunId;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    public void mergePartitionMetric(RepartitionWindowedMetricModel repartitionMetric, Integer partition) {
        double val = 0;
        switch (metricRunId.getAggregationType()) {
            case COUNT -> val = repartitionMetric.getValue();
            case LATENCY -> {
                val = repartitionMetric.getValue() / repartitionMetric.getNumberOfSamples();
            }
        }

        valuePerPartition.put(partition, val);
        sumPartitionValues();
    }

    private void sumPartitionValues() {
        switch (metricRunId.getAggregationType()) {
            case COUNT ->
                count = valuePerPartition.values().stream()
                        .mapToLong(Math::round)
                        .sum();
            case LATENCY -> {
                double latency = valuePerPartition.values().stream()
                        .mapToLong(Math::round)
                        .average()
                        .orElse(0);
                this.latencyAvg = Duration.ofNanos(Math.round(latency));
            }
            default -> throw new IllegalStateException("Unexpected value: " + metricRunId.getAggregationType());
        }
    }
}
