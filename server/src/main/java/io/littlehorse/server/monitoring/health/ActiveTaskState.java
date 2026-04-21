package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.TaskMetadata;

public class ActiveTaskState {
    private int partition;
    private Long processingLag;

    public ActiveTaskState() {}

    public ActiveTaskState(
            TaskMetadata meta, Map<TopicPartition, InProgressRestoration> restorations, LHServerConfig config) {
        Set<TopicPartition> topics = meta.topicPartitions();
        TopicPartition tp = topics.stream().findFirst().get();
        this.partition = tp.partition();
        Long endOffset = meta.endOffsets().get(tp);
        Long committedOffset = meta.committedOffsets().get(tp);
        this.processingLag = (endOffset == null || committedOffset == null) ? null : endOffset - committedOffset;
        // Note: If a Task is assigned to this Streams instance, and it's restoring, the task won't
        // show up in the ThreadMetadata#activeTasks(). That's a REALLY bad API in my opinion,
        // I'll tak to Matthias about making it better with a KIP.
    }

    public int getPartition() {
        return this.partition;
    }

    public Long getProcessingLag() {
        return this.processingLag;
    }

    public void setPartition(final int partition) {
        this.partition = partition;
    }

    public void setProcessingLag(final Long processingLag) {
        this.processingLag = processingLag;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ActiveTaskState)) return false;
        final ActiveTaskState other = (ActiveTaskState) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPartition() != other.getPartition()) return false;
        final Object this$processingLag = this.getProcessingLag();
        final Object other$processingLag = other.getProcessingLag();
        if (this$processingLag == null ? other$processingLag != null : !this$processingLag.equals(other$processingLag))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ActiveTaskState;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPartition();
        final Object $processingLag = this.getProcessingLag();
        result = result * PRIME + ($processingLag == null ? 43 : $processingLag.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ActiveTaskState(partition=" + this.getPartition() + ", processingLag=" + this.getProcessingLag() + ")";
    }
}
