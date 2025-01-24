package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MetricModel extends MetadataGetable<Metric> {

    private MetricIdModel id;
    private Date createdAt;
    private MeasurableObject measurable;
    private MetricType type;

    public MetricModel() {}

    public MetricModel(MetricIdModel id, MeasurableObject measurable, MetricType type) {
        this.id = id;
        this.createdAt = new Date();
        this.measurable = measurable;
        this.type = type;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        Metric p = (Metric) proto;
        id = LHSerializable.fromProto(p.getId(), MetricIdModel.class, context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        measurable = p.getMeasurable();
        type = p.getType();
    }

    @Override
    public Metric.Builder toProto() {
        return Metric.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setMeasurable(measurable)
                .setType(type);
    }

    @Override
    public MetricIdModel getObjectId() {
        return id;
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
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    @Override
    public Class<Metric> getProtoBaseClass() {
        return Metric.class;
    }
}
