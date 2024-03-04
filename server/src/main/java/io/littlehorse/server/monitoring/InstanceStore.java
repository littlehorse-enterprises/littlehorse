package io.littlehorse.server.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.apache.kafka.common.TopicPartition;

@Getter
final class InstanceStore {

    private final String storeName;
    private final Set<TopicPartitionMetrics> partitions = Collections.synchronizedSet(new HashSet<>());
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
}
