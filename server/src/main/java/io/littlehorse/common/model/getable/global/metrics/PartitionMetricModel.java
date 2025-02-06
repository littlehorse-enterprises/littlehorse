package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.sdk.common.proto.PartitionWindowedMetric;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionMetricModel extends CoreGetable<PartitionMetric> {
    private PartitionMetricIdModel id;
    private Date createdAt;
    private Set<PartitionWindowedMetricModel> activeWindowedMetrics;

    @Getter
    private double value;

    public PartitionMetricModel() {}

    public PartitionMetricModel(MetricIdModel metricId) {
        this.id = new PartitionMetricIdModel(metricId);
        this.createdAt = new Date();
        this.activeWindowedMetrics = null;
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
    public PartitionMetricIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public PartitionMetric.Builder toProto() {
        List<PartitionWindowedMetric> windowedMetrics = activeWindowedMetrics.stream()
                .map(PartitionWindowedMetricModel::toProto)
                .map(PartitionWindowedMetric.Builder::build)
                .toList();
        return PartitionMetric.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .addAllActiveWindows(windowedMetrics);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionMetric p = (PartitionMetric) proto;
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.id = LHSerializable.fromProto(p.getId(), PartitionMetricIdModel.class, context);
        this.activeWindowedMetrics = p.getActiveWindowsList().stream()
                .map(windowedMetric ->
                        LHSerializable.fromProto(windowedMetric, PartitionWindowedMetricModel.class, context))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Class<PartitionMetric> getProtoBaseClass() {
        return PartitionMetric.class;
    }

    public void incrementCurrentWindow() {
        currentWindow().increment();
    }

    private PartitionWindowedMetricModel currentWindow() {
        return activeWindowedMetrics.stream()
                .filter(PartitionWindowedMetricModel::windowClosed)
                .findFirst()
                .orElse(new PartitionWindowedMetricModel(0));
    }
}
