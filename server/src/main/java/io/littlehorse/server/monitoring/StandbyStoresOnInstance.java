package io.littlehorse.server.monitoring;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StandbyUpdateListener;

/**
 * Represents an instance-specific store that keeps track of topic partitions.
 * This class is responsible for managing and monitoring the topic partitions associated
 * with the current application instance.
 */
public final class StandbyStoresOnInstance {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StandbyStoresOnInstance.class);
    private final String storeName;
    private final Set<StandbyTopicPartitionMetrics> partitions = new HashSet<>();
    private final int clusterPartitions;

    StandbyStoresOnInstance(final String storeName, final int numberOfPartitionAssigned) {
        this.storeName = storeName;
        this.clusterPartitions = numberOfPartitionAssigned;
    }

    /**
     * Record offset tracking for the topic partition
     * @param topicPartition topic partition
     * @param currentOffset batch end offset
     * @param endOffset topic partition end offset
     */
    public synchronized void recordOffsets(
            TopicPartition topicPartition, final long currentOffset, final long endOffset) {
        StandbyTopicPartitionMetrics newMetric =
                new StandbyTopicPartitionMetrics(topicPartition, currentOffset, endOffset);
        partitions.remove(newMetric);
        partitions.add(newMetric);
    }

    /**
     * Calculates the total lag across all partitions in the store
     * @return sum of lag values for all partitions.
     */
    public synchronized long totalLag() {
        return // ignore sentinel values (-1)
        partitions.stream()
                .map(StandbyTopicPartitionMetrics::getCurrentLag)
                .map(lag -> Math.max(0, lag))
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * Retrieves the count of registered partitions associated with this store.
     *
     * @return The number of registered partitions for this store.
     */
    public synchronized int registeredPartitions() {
        return partitions.size();
    }

    public synchronized Optional<StandbyTopicPartitionMetrics> lagInfoForPartition(int partition) {
        return partitions.stream()
                .filter(standbyTopicPartitionMetrics -> standbyTopicPartitionMetrics.getPartition() == partition)
                .findFirst();
    }

    /**
     * Suspend offset tracking for the topic partition
     * @param topicPartition suspended partition
     * @param currentOffset last registered offset
     * @param endOffset partition end offset
     * @param reason standby suspension reason
     */
    public synchronized void suspendPartition(
            final TopicPartition topicPartition,
            final long currentOffset,
            final long endOffset,
            final StandbyUpdateListener.SuspendReason reason) {
        log.info("TopicPartition %s suspended with reason %s ".formatted(topicPartition, reason));
        partitions.remove(new StandbyTopicPartitionMetrics(topicPartition, currentOffset, endOffset));
    }

    public String getStoreName() {
        return this.storeName;
    }

    public Set<StandbyTopicPartitionMetrics> getPartitions() {
        return this.partitions;
    }

    public int getClusterPartitions() {
        return this.clusterPartitions;
    }
}
