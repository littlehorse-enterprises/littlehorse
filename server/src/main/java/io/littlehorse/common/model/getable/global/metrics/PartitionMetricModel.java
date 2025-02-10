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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionMetricModel extends CoreGetable<PartitionMetric> {
    private PartitionMetricIdModel id;
    private Date createdAt;
    private Set<PartitionWindowedMetricModel> activeWindowedMetrics;
    private Duration windowLength;

    public PartitionMetricModel() {}

    public PartitionMetricModel(MetricIdModel metricId, Duration windowLength) {
        this.id = new PartitionMetricIdModel(metricId);
        this.createdAt = new Date();
        this.activeWindowedMetrics = new TreeSet<>();
        this.windowLength = windowLength;
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
                .setWindowLength(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(windowLength.getSeconds())
                        .build())
                .addAllActiveWindows(windowedMetrics);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionMetric p = (PartitionMetric) proto;
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.id = LHSerializable.fromProto(p.getId(), PartitionMetricIdModel.class, context);
        this.activeWindowedMetrics = new TreeSet<>(p.getActiveWindowsList().stream()
                .map(windowedMetric ->
                        LHSerializable.fromProto(windowedMetric, PartitionWindowedMetricModel.class, context))
                .toList());
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
    }

    @Override
    public Class<PartitionMetric> getProtoBaseClass() {
        return PartitionMetric.class;
    }

    public void incrementCurrentWindow(LocalDateTime currentTime) {
        currentWindow(currentTime).increment();
    }

    private PartitionWindowedMetricModel currentWindow(LocalDateTime currentTime) {
        return activeWindowedMetrics.stream()
                .filter(Predicate.not(windowedMetric -> windowedMetric.windowClosed(windowLength, currentTime)))
                .findFirst()
                .orElse(createAndAppendWindow(currentTime));
    }

    private PartitionWindowedMetricModel createAndAppendWindow(LocalDateTime currentTime) {
        long millis = currentTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        PartitionWindowedMetricModel newWindow = new PartitionWindowedMetricModel(0, millis, windowLength);
        activeWindowedMetrics.add(newWindow);
        return newWindow;
    }

    Set<PartitionWindowedMetricModel> getActiveWindowedMetrics() {
        return activeWindowedMetrics;
    }
}
