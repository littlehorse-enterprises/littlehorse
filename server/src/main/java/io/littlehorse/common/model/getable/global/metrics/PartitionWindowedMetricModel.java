package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.PartitionWindowedMetric;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionWindowedMetricModel extends LHSerializable<PartitionWindowedMetric>
        implements Comparable<PartitionWindowedMetricModel> {
    private double value;
    private long numberOfSamples;
    private LocalDateTime windowStart;

    public PartitionWindowedMetricModel() {}

    public PartitionWindowedMetricModel(double initialValue, long currentTimeMillis, Duration windowLength) {
        this.numberOfSamples = 0L;
        this.value = initialValue;
        this.windowStart = LocalDateTime.ofInstant(
                LHUtil.getWindowStart(currentTimeMillis, windowLength).toInstant(), ZoneId.systemDefault());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PartitionWindowedMetric p = (PartitionWindowedMetric) proto;
        this.windowStart =
                LocalDateTime.ofInstant(LHUtil.fromProtoTs(p.getWindowStart()).toInstant(), ZoneId.systemDefault());
        this.value = p.getValue();
        this.numberOfSamples = p.getNumberOfSamples();
    }

    @Override
    public PartitionWindowedMetric.Builder toProto() {
        return PartitionWindowedMetric.newBuilder()
                .setValue(value)
                .setWindowStart(LHUtil.fromDate(
                        Date.from(windowStart.atZone(ZoneId.systemDefault()).toInstant())))
                .setNumberOfSamples(numberOfSamples);
    }

    @Override
    public Class<PartitionWindowedMetric> getProtoBaseClass() {
        return PartitionWindowedMetric.class;
    }

    @Override
    public int compareTo(PartitionWindowedMetricModel o) {
        return o.windowStart.compareTo(windowStart);
    }

    boolean windowClosed(Duration windowLength, LocalDateTime currentTime) {
        long elapsed = Duration.between(windowStart, currentTime).toMillis();

        return elapsed > windowLength.toMillis();
    }

    public void increment(double increment) {
        numberOfSamples++;
        value = value + increment;
    }

    public double getValue() {
        return value;
    }

    Date getWindowStart() {
        return Date.from(windowStart.atZone(ZoneId.systemDefault()).toInstant());
    }

    public long getNumberOfSamples() {
        return numberOfSamples;
    }
}
