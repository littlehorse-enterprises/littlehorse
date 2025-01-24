package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.metrics.MetricModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class MetricIdModel extends MetadataId<MetricId, Metric, MetricModel> {

    private String id;

    public MetricIdModel() {}
    public MetricIdModel(String id) {
        this.id = id;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricId p = (MetricId) proto;
        this.id = p.getId();
    }

    @Override
    public MetricId.Builder toProto() {
        return MetricId.newBuilder().setId(id);
    }

    @Override
    public Class<MetricId> getProtoBaseClass() {
        return MetricId.class;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        this.id = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC;
    }
}
