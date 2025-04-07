package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.sdk.common.proto.PartitionMetricId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class PartitionMetricIdModel extends CoreObjectId<PartitionMetricId, PartitionMetric, PartitionMetricModel> {

    private MetricSpecIdModel metricId;
    private TenantIdModel tenantId;
    private AggregationType aggregationType;

    public PartitionMetricIdModel() {}

    public PartitionMetricIdModel(
            MetricSpecIdModel partitionMetricId, TenantIdModel tenantId, AggregationType aggregationType) {
        this.metricId = partitionMetricId;
        this.tenantId = tenantId;
        this.aggregationType = aggregationType;
    }

    @Override
    public PartitionMetricId.Builder toProto() {
        return PartitionMetricId.newBuilder()
                .setId(metricId.toProto())
                .setTenantId(tenantId.toProto())
                .setAggregationType(aggregationType);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionMetricId partitionMetricId = (PartitionMetricId) proto;
        this.metricId = LHSerializable.fromProto(partitionMetricId.getId(), MetricSpecIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(partitionMetricId.getTenantId(), TenantIdModel.class, context);
        this.aggregationType = partitionMetricId.getAggregationType();
    }

    @Override
    public Class<PartitionMetricId> getProtoBaseClass() {
        return PartitionMetricId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(aggregationType.name(), metricId.toString(), tenantId.toString());
    }

    @Override
    public void initFromString(String storeKey) {
        String[] keyParts = storeKey.split("/");
        this.aggregationType = AggregationType.valueOf(keyParts[0]);
        this.metricId = new MetricSpecIdModel();
        this.metricId.initFromString(keyParts[1]);
        this.tenantId = new TenantIdModel();
        this.tenantId.initFromString(keyParts[2]);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.PARTITION_METRIC;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.empty();
    }
}
