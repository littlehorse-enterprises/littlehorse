package io.littlehorse.server.monitoring;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.kafka.common.TopicPartition;

@Getter
@EqualsAndHashCode(of = "partition")
class TopicPartitionMetrics {

    private final TopicPartition partition;
    private final long currentOffset;
    private final long endOffset;
    private final long currentLag;

    public TopicPartitionMetrics(final TopicPartition partition, final long currentOffset, final long endOffset) {
        this.partition = partition;
        this.currentOffset = currentOffset;
        this.endOffset = endOffset;
        this.currentLag = endOffset < currentOffset ? -1L : this.endOffset - currentOffset;
    }



}
