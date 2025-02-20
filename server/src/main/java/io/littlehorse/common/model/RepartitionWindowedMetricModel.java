package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.RepartitionWindowedMetric;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class RepartitionWindowedMetricModel extends LHSerializable<RepartitionWindowedMetric> {

    private double value;
    private double numberOfSamples;
    private Date windowStart;

    public RepartitionWindowedMetricModel() {}

    public RepartitionWindowedMetricModel(double value, double numberOfSamples, Date windowStart) {
        this.value = value;
        this.windowStart = windowStart;
        this.numberOfSamples = numberOfSamples;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        RepartitionWindowedMetric p = (RepartitionWindowedMetric) proto;
        this.value = p.getValue();
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        this.numberOfSamples = p.getNumberOfSamples();
    }

    @Override
    public RepartitionWindowedMetric.Builder toProto() {
        return RepartitionWindowedMetric.newBuilder()
                .setValue(value)
                .setWindowStart(LHUtil.fromDate(windowStart))
                .setNumberOfSamples(numberOfSamples);
    }

    @Override
    public Class<RepartitionWindowedMetric> getProtoBaseClass() {
        return RepartitionWindowedMetric.class;
    }
}
