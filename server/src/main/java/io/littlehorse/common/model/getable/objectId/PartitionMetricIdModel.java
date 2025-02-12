package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.sdk.common.proto.PartitionMetricId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

@Getter
public class PartitionMetricIdModel extends CoreObjectId<PartitionMetricId, PartitionMetric, PartitionMetricModel> {

    private MetricIdModel metricId;
    private TenantIdModel tenantId;

    public PartitionMetricIdModel() {}

    public PartitionMetricIdModel(MetricIdModel partitionMetricId, TenantIdModel tenantId) {
        this.metricId = partitionMetricId;
        this.tenantId = tenantId;
    }

    @Override
    public PartitionMetricId.Builder toProto() {
        return PartitionMetricId.newBuilder().setId(metricId.toProto()).setTenantId(tenantId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionMetricId partitionMetricId = (PartitionMetricId) proto;
        this.metricId = LHSerializable.fromProto(partitionMetricId.getId(), MetricIdModel.class, context);
        this.tenantId = LHSerializable.fromProto(partitionMetricId.getTenantId(), TenantIdModel.class, context);
    }

    @Override
    public Class<PartitionMetricId> getProtoBaseClass() {
        return PartitionMetricId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(metricId.toString(), tenantId.toString());
    }

    @Override
    public void initFromString(String storeKey) {
        String[] keyParts = storeKey.split("/");
        this.metricId = new MetricIdModel();
        this.metricId.initFromString(keyParts[0]);
        this.tenantId = new TenantIdModel();
        this.tenantId.initFromString(keyParts[1]);
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
