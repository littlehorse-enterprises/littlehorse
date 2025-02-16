package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.proto.RepartitionWindowedMetric;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class RepartitionWindowedMetricModel extends LHSerializable<RepartitionWindowedMetric> {

    private MetricIdModel metricId;
    private double value;
    private Date windowStart;

    public RepartitionWindowedMetricModel() {}

    public RepartitionWindowedMetricModel(MetricIdModel metricId, double value, Date windowStart) {
        this.metricId = metricId;
        this.value = value;
        this.windowStart = windowStart;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        RepartitionWindowedMetric p = (RepartitionWindowedMetric) proto;
        this.metricId = LHSerializable.fromProto(p.getMetricId(), MetricIdModel.class, context);
        this.value = p.getValue();
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
    }

    @Override
    public RepartitionWindowedMetric.Builder toProto() {
        return RepartitionWindowedMetric.newBuilder()
                .setMetricId(metricId.toProto())
                .setValue(value)
                .setWindowStart(LHUtil.fromDate(windowStart));
    }

    @Override
    public Class<RepartitionWindowedMetric> getProtoBaseClass() {
        return RepartitionWindowedMetric.class;
    }
}
