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
import org.jetbrains.annotations.NotNull;

public class PartitionWindowedMetricModel extends LHSerializable<PartitionWindowedMetric>
        implements Comparable<PartitionWindowedMetricModel> {

    private double value;
    private LocalDateTime windowStart;

    public PartitionWindowedMetricModel() {}

    public PartitionWindowedMetricModel(double initialValue) {
        this.value = initialValue;
        this.windowStart = LocalDateTime.ofInstant(
                LHUtil.getWindowStart(new Date(), Duration.ofMinutes(1)).toInstant(), ZoneId.systemDefault());
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
    public int compareTo(@NotNull PartitionWindowedMetricModel o) {
        return o.windowStart.compareTo(windowStart);
    }

    boolean windowClosed() {
        long elapsed = Duration.between(windowStart, LocalDateTime.now()).toMillis();
        return elapsed > Duration.ofMinutes(1).toMillis();
    }

    public void increment() {
        value++;
    }
}
