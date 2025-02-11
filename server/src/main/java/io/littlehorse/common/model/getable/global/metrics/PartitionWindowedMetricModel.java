package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
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
    private LocalDateTime windowStart;

    public PartitionWindowedMetricModel() {}

    public PartitionWindowedMetricModel(double initialValue, long currentTimeMillis, Duration windowLength) {
        this.value = initialValue;
        this.windowStart = LocalDateTime.ofInstant(
                LHUtil.getWindowStart(currentTimeMillis, windowLength).toInstant(), ZoneId.systemDefault());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        PartitionWindowedMetric p = (PartitionWindowedMetric) proto;
        this.windowStart =
                LocalDateTime.ofInstant(LHUtil.fromProtoTs(p.getWindowStart()).toInstant(), ZoneId.systemDefault());
        this.value = p.getValue();
    }

    @Override
    public PartitionWindowedMetric.Builder toProto() {
        return PartitionWindowedMetric.newBuilder()
                .setValue(value)
                .setWindowStart(LHUtil.fromDate(
                        Date.from(windowStart.atZone(ZoneId.systemDefault()).toInstant())));
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
        boolean closed = elapsed > windowLength.toMillis();
        if (closed) {
            log.info("Window closed, creating a new one. Elapsed time: {} ms", elapsed);
        }
        return closed;
    }

    public void increment() {
        value++;
    }

    double getValue() {
        return value;
    }

    Date getWindowStart() {
        return Date.from(windowStart.atZone(ZoneId.systemDefault()).toInstant());
    }
}
