package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.RepartitionedId;
import io.littlehorse.common.model.getable.core.metrics.MetricModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MetricIdModel extends RepartitionedId<MetricId, Metric, MetricModel> {

    private MetricSpecIdModel metricSpecId;
    private Date windowStart;
    private Duration windowLength;
    private AggregationType aggregationType;

    public MetricIdModel() {}

    public MetricIdModel(
            MetricSpecIdModel metricSpecId, Date windowStart, Duration windowLength, AggregationType aggregationType) {
        this.metricSpecId = metricSpecId;
        this.windowStart = windowStart;
        this.windowLength = windowLength;
        this.aggregationType = aggregationType;
    }

    @Override
    public MetricId.Builder toProto() {
        return MetricId.newBuilder()
                .setMetricSpecId(metricSpecId.toProto())
                .setWindowStart(LHUtil.fromDate(windowStart))
                .setWindowLength(com.google.protobuf.Duration.newBuilder().setSeconds(windowLength.getSeconds()))
                .setAggregationType(aggregationType);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        MetricId p = (MetricId) proto;
        this.metricSpecId = LHSerializable.fromProto(p.getMetricSpecId(), MetricSpecIdModel.class, context);
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
        this.aggregationType = p.getAggregationType();
    }

    @Override
    public Class<MetricId> getProtoBaseClass() {
        return MetricId.class;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(
                this.metricSpecId.toString(),
                this.aggregationType.name(),
                String.valueOf(windowLength.getSeconds()),
                String.valueOf(windowStart.getTime()));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] parts = storeKey.split("/");
        this.metricSpecId = (MetricSpecIdModel)
                MetricSpecIdModel.fromString(LHUtil.getCompositeId(parts[0], parts[1]), MetricSpecIdModel.class);
        this.aggregationType = AggregationType.valueOf(parts[2]);
        this.windowStart = new Date(Long.parseLong(parts[3]));
        this.windowLength = Duration.ofSeconds(Long.parseLong(parts[4]));
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.METRIC;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return this.metricSpecId.getPartitionKey();
    }
}
