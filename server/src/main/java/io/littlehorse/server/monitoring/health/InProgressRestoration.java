package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InProgressRestoration that = (InProgressRestoration) o;
        return partition == that.partition
                && totalRestored == that.totalRestored
                && endOffset == that.endOffset
                && currentOffset == that.currentOffset
                && Objects.equals(topic, that.topic)
                && processor == that.processor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, partition, processor, totalRestored, endOffset, currentOffset);
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
}
