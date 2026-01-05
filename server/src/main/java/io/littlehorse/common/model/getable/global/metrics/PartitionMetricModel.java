package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.RepartitionWindowedMetricModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.sdk.common.proto.PartitionWindowedMetric;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class PartitionMetricModel extends CoreGetable<PartitionMetric> {
    private PartitionMetricIdModel id;
    private Date createdAt;
    private Set<PartitionWindowedMetricModel> activeWindowedMetrics;
    private Duration windowLength;

    public PartitionMetricModel() {}

    public PartitionMetricModel(PartitionMetricIdModel partitionId, Duration windowLength) {
        this.id = partitionId;
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
    public void initFrom(Message proto, ExecutionContext context) {
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

    public void incrementCurrentWindow(LocalDateTime currentTime, double increment) {
        currentWindow(currentTime).increment(increment);
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

    public Set<PartitionWindowedMetricModel> getActiveWindowedMetrics() {
        return Collections.unmodifiableSet(activeWindowedMetrics);
    }

    public List<RepartitionWindowedMetricModel> buildRepartitionCommand(LocalDateTime currentTime) {
        if (activeWindowedMetrics.isEmpty()) {
            return List.of();
        }
        List<RepartitionWindowedMetricModel> windowedMetrics =
                activeWindowedMetrics.stream().map(this::toRepartitionMetric).toList();
        activeWindowedMetrics.removeIf(windowedMetric -> windowedMetric.windowClosed(windowLength, currentTime));
        return windowedMetrics;
    }

    private RepartitionWindowedMetricModel toRepartitionMetric(PartitionWindowedMetricModel windowedMetric) {
        return new RepartitionWindowedMetricModel(
                windowedMetric.getValue(),
                windowedMetric.getNumberOfSamples(),
                windowedMetric.getWindowStart(),
                windowLength);
    }
}
