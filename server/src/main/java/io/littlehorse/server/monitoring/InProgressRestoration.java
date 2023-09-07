package io.littlehorse.server.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.health.LHProcessorType;
import io.littlehorse.server.monitoring.health.ServerHealthState;
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

    @JsonIgnore
    public long getRemaining() {
        return endOffset - currentOffset;
    }

    public void onBatchRestored(long batchEndOffset, long numRestored) {
        totalRestored += numRestored;
        currentOffset = batchEndOffset;
    }
}
