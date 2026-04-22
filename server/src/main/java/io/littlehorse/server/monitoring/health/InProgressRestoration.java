package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.TopicPartition;

/**
 * Used by the StreamsHealthWatcher to represent the state of an
 * in-progress restoration (whether for an active or standby task).
 */
@Getter
@Setter
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
}
