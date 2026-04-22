package io.littlehorse.server.monitoring;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import org.apache.kafka.common.TopicPartition;

@Getter
public class StandbyTopicPartitionMetrics implements Serializable {

    private final int partition;
    private final String topic;
    private final long currentOffset;
    private final long endOffset;
    private final long currentLag;

    public StandbyTopicPartitionMetrics(
            final TopicPartition partition, final long currentOffset, final long endOffset) {
        this.partition = partition.partition();
        this.topic = partition.topic();
        this.currentOffset = currentOffset;
        this.endOffset = endOffset;
        this.currentLag = endOffset < currentOffset ? -1L : this.endOffset - currentOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandbyTopicPartitionMetrics that = (StandbyTopicPartitionMetrics) o;
        return partition == that.partition && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partition, topic);
    }
}
