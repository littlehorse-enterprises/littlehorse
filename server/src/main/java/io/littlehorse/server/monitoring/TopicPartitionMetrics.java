package io.littlehorse.server.monitoring;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.kafka.common.TopicPartition;

@Getter
@EqualsAndHashCode(of = {"partition", "topic"})
class TopicPartitionMetrics implements Serializable {

    private final int partition;
    private final String topic;
    private final long currentOffset;
    private final long endOffset;
    private final long currentLag;

    public TopicPartitionMetrics(final TopicPartition partition, final long currentOffset, final long endOffset) {
        this.partition = partition.partition();
        this.topic = partition.topic();
        this.currentOffset = currentOffset;
        this.endOffset = endOffset;
        this.currentLag = endOffset < currentOffset ? -1L : this.endOffset - currentOffset;
    }
}
