package io.littlehorse.server.monitoring;

import java.io.Serializable;
import org.apache.kafka.common.TopicPartition;

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

    public int getPartition() {
        return this.partition;
    }

    public String getTopic() {
        return this.topic;
    }

    public long getCurrentOffset() {
        return this.currentOffset;
    }

    public long getEndOffset() {
        return this.endOffset;
    }

    public long getCurrentLag() {
        return this.currentLag;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof StandbyTopicPartitionMetrics)) return false;
        final StandbyTopicPartitionMetrics other = (StandbyTopicPartitionMetrics) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPartition() != other.getPartition()) return false;
        final Object this$topic = this.getTopic();
        final Object other$topic = other.getTopic();
        if (this$topic == null ? other$topic != null : !this$topic.equals(other$topic)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StandbyTopicPartitionMetrics;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPartition();
        final Object $topic = this.getTopic();
        result = result * PRIME + ($topic == null ? 43 : $topic.hashCode());
        return result;
    }
}
