package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.RepartitionWindowedMetricModel;
import io.littlehorse.common.model.RepartitionedGetable;
import io.littlehorse.common.model.getable.objectId.MetricRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MetricRun;
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
public class MetricRunModel extends RepartitionedGetable<MetricRun> {

    private MetricRunIdModel metricRunId;
    private Date createdAt;
    private Long count;
    private Duration latencyAvg;
    private Map<Integer, Double> valuePerPartition = new HashMap<>();

    public MetricRunModel() {}

    public MetricRunModel(MetricRunIdModel metricRunId) {
        this.metricRunId = metricRunId;
        this.createdAt = new Date();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricRun p = (MetricRun) proto;
        this.metricRunId = LHSerializable.fromProto(p.getId(), MetricRunIdModel.class, context);
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        switch (p.getValueCase()) {
            case COUNT -> this.count = p.getCount();
            case LATENCY_AVG -> this.latencyAvg = Duration.ofMillis(p.getLatencyAvg());
            default -> throw new IllegalStateException("Unexpected value: " + p.getValueCase());
        }
        this.valuePerPartition = new HashMap<>(p.getValuePerPartitionMap());
    }

    @Override
    public MetricRun.Builder toProto() {
        MetricRun.Builder out = MetricRun.newBuilder()
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
    public Class<MetricRun> getProtoBaseClass() {
        return MetricRun.class;
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
    public MetricRunIdModel getObjectId() {
        return metricRunId;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    public void mergePartitionMetric(RepartitionWindowedMetricModel repartitionMetric, Integer partition) {
        double val = 0;
        switch (metricRunId.getMetricId().getMetricType()) {
            case COUNT -> val = valuePerPartition.values().stream()
                    .mapToDouble(val2 -> val2)
                    .sum();
            case LATENCY -> {
                val = repartitionMetric.getValue() / repartitionMetric.getNumberOfSamples();
            }
        }

        valuePerPartition.put(partition, val);
        sumPartitionValues();
    }

    private void sumPartitionValues() {
        switch (metricRunId.getMetricId().getMetricType()) {
            case COUNT -> count =
                    valuePerPartition.values().stream().mapToLong(Math::round).sum();
            case LATENCY -> {
                double latency = valuePerPartition.values().stream()
                        .mapToLong(Math::round)
                        .average()
                        .orElse(0);
                this.latencyAvg = Duration.ofNanos(Math.round(latency));
                log.info("Sum latency avg: {}", latencyAvg.toMillis());
            }
            default -> throw new IllegalStateException(
                    "Unexpected value: " + metricRunId.getMetricId().getMetricType());
        }
    }
}
