package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.metrics.MetricModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricId;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MetricIdModel extends MetadataId<MetricId, Metric, MetricModel> {

    private MeasurableObject measurable;
    private MetricType metricType;

    public MetricIdModel() {}

    public MetricIdModel(MeasurableObject measurable, MetricType type) {
        this.measurable = measurable;
        this.metricType = type;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricId p = (MetricId) proto;
        this.measurable = p.getMeasurable();
        this.metricType = p.getType();
    }

    @Override
    public MetricId.Builder toProto() {
        return MetricId.newBuilder().setMeasurable(measurable).setType(metricType);
    }

    @Override
    public Class<MetricId> getProtoBaseClass() {
        return MetricId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(measurable.toString(), metricType.toString());
    }

    @Override
    public void initFromString(String storeKey) {
        MeasurableObject measurable = MeasurableObject.valueOf(storeKey.split("/")[0]);
        MetricType type = MetricType.valueOf(storeKey.split("/")[1]);
        this.measurable = measurable;
        this.metricType = type;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(LHUtil.getCompositeId(measurable.toString(), metricType.toString()));
    }
}
