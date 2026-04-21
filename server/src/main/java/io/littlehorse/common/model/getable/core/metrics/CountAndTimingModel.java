package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CountAndTimingModel extends LHSerializable<CountAndTiming> {
    private int count;
    private long minLatencyMs;
    private long maxLatencyMs;
    private long totalLatencyMs;

    CountAndTimingModel(int count, long minLatencyMs, long maxLatencyMs, long totalLatencyMs) {
        this.count = count;
        this.minLatencyMs = minLatencyMs;
        this.maxLatencyMs = maxLatencyMs;
        this.totalLatencyMs = totalLatencyMs;
    }

    public void incrementCountAndLatency(long incomingLatencyMs) {
        this.count++;
        this.totalLatencyMs += incomingLatencyMs;
        if (this.minLatencyMs == 0 || incomingLatencyMs < this.minLatencyMs) {
            this.minLatencyMs = incomingLatencyMs;
        }
        if (incomingLatencyMs > this.maxLatencyMs) {
            this.maxLatencyMs = incomingLatencyMs;
        }
    }

    public void incrementCount() {
        this.count++;
    }

    public void mergeFrom(CountAndTimingModel other) {
        this.count += other.count;
        this.minLatencyMs = Math.min(this.minLatencyMs, other.minLatencyMs);
        this.maxLatencyMs = Math.max(this.maxLatencyMs, other.maxLatencyMs);
        this.totalLatencyMs += other.totalLatencyMs;
    }

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

    public int getCount() {
        return this.count;
    }

    public long getMinLatencyMs() {
        return this.minLatencyMs;
    }

    public long getMaxLatencyMs() {
        return this.maxLatencyMs;
    }

    public long getTotalLatencyMs() {
        return this.totalLatencyMs;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public void setMinLatencyMs(final long minLatencyMs) {
        this.minLatencyMs = minLatencyMs;
    }

    public void setMaxLatencyMs(final long maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public void setTotalLatencyMs(final long totalLatencyMs) {
        this.totalLatencyMs = totalLatencyMs;
    }

    public CountAndTimingModel() {}
}
