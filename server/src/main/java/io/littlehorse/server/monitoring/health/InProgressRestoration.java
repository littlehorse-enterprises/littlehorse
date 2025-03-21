package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;

/**
 * Used by the StreamsHealthWatcher to represent the state of an
 * in-progress restoration (whether for an active or standby task).
 */
@Data
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
}
