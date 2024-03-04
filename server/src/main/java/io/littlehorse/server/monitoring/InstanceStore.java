package io.littlehorse.server.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StandbyUpdateListener;

@Getter
@Slf4j
final class InstanceStore {

    private final String storeName;
    private final Set<TopicPartitionMetrics> partitions = new HashSet<>();
    private final int clusterPartitions;

    public InstanceStore(final String storeName, final int numberOfPartitionAssigned) {
        this.storeName = storeName;
        this.clusterPartitions = numberOfPartitionAssigned;
    }

    public void recordOffsets(TopicPartition topicPartition, final long currentOffset, final long endOffset) {
        partitions.add(new TopicPartitionMetrics(topicPartition, currentOffset, endOffset));
    }

    @JsonProperty("totalLag")
    public long totalLag() {
        return partitions.stream()
                .map(TopicPartitionMetrics::getCurrentLag)
                .map(lag -> Math.max(0, lag)) // ignore sentinel values (-1)
                .mapToLong(Long::longValue)
                .sum();
    }

    @JsonProperty("numberOfRegisteredPartitions")
    public long registeredPartitions() {
        return partitions.size();
    }

    public void suspendPartition(
            TopicPartition topicPartition,
            final long currentOffset,
            final long endOffset,
            StandbyUpdateListener.SuspendReason reason) {
        log.info("TopicPartition %s suspended with reason %s ".formatted(topicPartition, reason));
        partitions.remove(new TopicPartitionMetrics(topicPartition, currentOffset, endOffset));
    }
}
