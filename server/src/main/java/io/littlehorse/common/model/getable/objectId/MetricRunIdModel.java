package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.RepartitionedId;
import io.littlehorse.common.model.getable.core.metrics.MetricRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.MetricRun;
import io.littlehorse.sdk.common.proto.MetricRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MetricRunIdModel extends RepartitionedId<MetricRunId, MetricRun, MetricRunModel> {

    private MetricIdModel metricId;
    private Date windowStart;

    public MetricRunIdModel() {}

    public MetricRunIdModel(MetricIdModel metricId, Date windowStart) {
        this.metricId = metricId;
        this.windowStart = windowStart;
    }

    @Override
    public MetricRunId.Builder toProto() {
        return MetricRunId.newBuilder().setMetricId(metricId.toProto()).setWindowStart(LHUtil.fromDate(windowStart));
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricRunId p = (MetricRunId) proto;
        this.metricId = LHSerializable.fromProto(p.getMetricId(), MetricIdModel.class, context);
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    @Override
    public Class<MetricRunId> getProtoBaseClass() {
        return MetricRunId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(this.metricId.toString(), String.valueOf(windowStart.getTime()));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] parts = storeKey.split("/");
        this.metricId = (MetricIdModel)
                MetricIdModel.fromString(LHUtil.getCompositeId(parts[0], parts[1]), MetricIdModel.class);
        this.windowStart = new Date(Long.parseLong(parts[2]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC_RUN;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return this.metricId.getPartitionKey();
    }
}
