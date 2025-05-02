package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.RepartitionWindowedMetric;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.util.Date;
import lombok.Getter;

@Getter
public class RepartitionWindowedMetricModel extends LHSerializable<RepartitionWindowedMetric> {

    private double value;
    private double numberOfSamples;
    private Date windowStart;
    private Duration windowLength;

    public RepartitionWindowedMetricModel() {}

    public RepartitionWindowedMetricModel(
            double value, double numberOfSamples, Date windowStart, Duration windowLength) {
        this.value = value;
        this.windowStart = windowStart;
        this.numberOfSamples = numberOfSamples;
        this.windowLength = windowLength;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        RepartitionWindowedMetric p = (RepartitionWindowedMetric) proto;
        this.value = p.getValue();
        this.windowStart = LHUtil.fromProtoTs(p.getWindowStart());
        this.numberOfSamples = p.getNumberOfSamples();
        this.windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
    }

    @Override
    public RepartitionWindowedMetric.Builder toProto() {
        return RepartitionWindowedMetric.newBuilder()
                .setValue(value)
                .setWindowStart(LHUtil.fromDate(windowStart))
                .setNumberOfSamples(numberOfSamples)
                .setWindowLength(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(windowLength.getSeconds())
                        .build());
    }

    @Override
    public Class<RepartitionWindowedMetric> getProtoBaseClass() {
        return RepartitionWindowedMetric.class;
    }
}
