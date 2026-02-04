package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountAndTimingModel extends LHSerializable<CountAndTiming> {
    private int count;
    private long minLatencyMs;
    private long maxLatencyMs;
    private long totalLatencyMs;

    @Override
    public Class<CountAndTiming> getProtoBaseClass() {
        return CountAndTiming.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        CountAndTiming p = (CountAndTiming) proto;
        count = p.getCount();
        minLatencyMs = p.getMinLatencyMs();
        maxLatencyMs = p.getMaxLatencyMs();
        totalLatencyMs = p.getTotalLatencyMs();
    }

    @Override
    public CountAndTiming.Builder toProto() {
        return CountAndTiming.newBuilder()
                .setCount(count)
                .setMinLatencyMs(minLatencyMs)
                .setMaxLatencyMs(maxLatencyMs)
                .setTotalLatencyMs(totalLatencyMs);
    }

    public void add(int count, long latencyMs) {
        this.count += count;
        if (this.minLatencyMs == 0 || latencyMs < this.minLatencyMs) {
            this.minLatencyMs = latencyMs;
        }
        if (latencyMs > this.maxLatencyMs) {
            this.maxLatencyMs = latencyMs;
        }
        this.totalLatencyMs += latencyMs * count;
    }

    public void add(int count) {
        this.count += count;
    }

    public void mergeFrom(CountAndTimingModel other) {
        this.count += other.count;
        this.minLatencyMs = Math.min(this.minLatencyMs, other.minLatencyMs);
        this.maxLatencyMs = Math.max(this.maxLatencyMs, other.maxLatencyMs);
        this.totalLatencyMs += other.totalLatencyMs;
    }
}
