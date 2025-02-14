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
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MetricRunModel extends RepartitionedGetable<MetricRun> {

    private MetricRunIdModel metricRunId;
    private Date createdAt;
    private double value;

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
        this.value = p.getValue();
    }

    @Override
    public MetricRun.Builder toProto() {
        return MetricRun.newBuilder()
                .setId(metricRunId.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setValue(value);
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

    public void mergePartitionMetric(RepartitionWindowedMetricModel repartitionMetric) {
        value += repartitionMetric.getValue();
    }
}
