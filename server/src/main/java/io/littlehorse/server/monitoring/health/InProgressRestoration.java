package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import org.apache.kafka.common.TopicPartition;

/**
 * Used by the StreamsHealthWatcher to represent the state of an
 * in-progress restoration (whether for an active or standby task).
 */
public class InProgressRestoration {
    String topic;
    int partition;
    LHProcessorType processor;
    long totalRestored;
    long endOffset;
    long currentOffset;

    public InProgressRestoration() {}

    public InProgressRestoration(
            TopicPartition tp, String storeName, long startOffset, long endOffset, LHServerConfig config) {
        this.topic = tp.topic();
        this.partition = tp.partition();
        this.processor = ServerHealthState.fromTopic(topic, config);
        this.currentOffset = startOffset;
        this.endOffset = endOffset;
    }

    public long getRemaining() {
        return endOffset - currentOffset;
    }

    public void onBatchRestored(long batchEndOffset, long numRestored) {
        totalRestored += numRestored;
        currentOffset = batchEndOffset;
    }

    public String getTopic() {
        return this.topic;
    }

    public int getPartition() {
        return this.partition;
    }

    public LHProcessorType getProcessor() {
        return this.processor;
    }

    public long getTotalRestored() {
        return this.totalRestored;
    }

    public long getEndOffset() {
        return this.endOffset;
    }

    public long getCurrentOffset() {
        return this.currentOffset;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public void setPartition(final int partition) {
        this.partition = partition;
    }

    public void setProcessor(final LHProcessorType processor) {
        this.processor = processor;
    }

    public void setTotalRestored(final long totalRestored) {
        this.totalRestored = totalRestored;
    }

    public void setEndOffset(final long endOffset) {
        this.endOffset = endOffset;
    }

    public void setCurrentOffset(final long currentOffset) {
        this.currentOffset = currentOffset;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof InProgressRestoration)) return false;
        final InProgressRestoration other = (InProgressRestoration) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPartition() != other.getPartition()) return false;
        if (this.getTotalRestored() != other.getTotalRestored()) return false;
        if (this.getEndOffset() != other.getEndOffset()) return false;
        if (this.getCurrentOffset() != other.getCurrentOffset()) return false;
        final Object this$topic = this.getTopic();
        final Object other$topic = other.getTopic();
        if (this$topic == null ? other$topic != null : !this$topic.equals(other$topic)) return false;
        final Object this$processor = this.getProcessor();
        final Object other$processor = other.getProcessor();
        if (this$processor == null ? other$processor != null : !this$processor.equals(other$processor)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof InProgressRestoration;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPartition();
        final long $totalRestored = this.getTotalRestored();
        result = result * PRIME + (int) ($totalRestored >>> 32 ^ $totalRestored);
        final long $endOffset = this.getEndOffset();
        result = result * PRIME + (int) ($endOffset >>> 32 ^ $endOffset);
        final long $currentOffset = this.getCurrentOffset();
        result = result * PRIME + (int) ($currentOffset >>> 32 ^ $currentOffset);
        final Object $topic = this.getTopic();
        result = result * PRIME + ($topic == null ? 43 : $topic.hashCode());
        final Object $processor = this.getProcessor();
        result = result * PRIME + ($processor == null ? 43 : $processor.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "InProgressRestoration(topic=" + this.getTopic() + ", partition=" + this.getPartition() + ", processor="
                + this.getProcessor() + ", totalRestored=" + this.getTotalRestored() + ", endOffset="
                + this.getEndOffset() + ", currentOffset=" + this.getCurrentOffset() + ")";
    }
}
