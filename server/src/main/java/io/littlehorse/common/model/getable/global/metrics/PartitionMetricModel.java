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
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionMetricModel extends CoreGetable<PartitionMetric> {
    private PartitionMetricIdModel id;
    private Date createdAt;
    private LocalDateTime windowStart;

    @Getter
    private double value;

    public PartitionMetricModel() {}

    public PartitionMetricModel(MetricIdModel metricId) {
        log.info("Creating PartitionMetricModel");
        this.id = new PartitionMetricIdModel(metricId);
        this.createdAt = new Date();
        this.windowStart = LocalDateTime.ofInstant(
                LHUtil.getWindowStart(this.createdAt, Duration.ofMinutes(1)).toInstant(), ZoneId.systemDefault());
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
        return PartitionMetric.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setValue(value)
                .setWindowStart(LHUtil.fromDate(
                        Date.from(windowStart.atZone(ZoneId.systemDefault()).toInstant())));
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionMetric p = (PartitionMetric) proto;
        this.createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        this.id = LHSerializable.fromProto(p.getId(), PartitionMetricIdModel.class, context);
        this.value = p.getValue();
        this.windowStart =
                LocalDateTime.ofInstant(LHUtil.fromProtoTs(p.getWindowStart()).toInstant(), ZoneId.systemDefault());
    }

    @Override
    public Class<PartitionMetric> getProtoBaseClass() {
        return PartitionMetric.class;
    }

    public void increment() {
        if (windowClosed()) {
            value = 0;
            windowStart = LocalDateTime.ofInstant(
                    LHUtil.getWindowStart(new Date(), Duration.ofMinutes(1)).toInstant(), ZoneId.systemDefault());
        }
        value++;
    }

    private boolean windowClosed() {
        long elapsed = Duration.between(windowStart, LocalDateTime.now()).toMillis();
        return elapsed > Duration.ofMinutes(1).toMillis();
    }
}
