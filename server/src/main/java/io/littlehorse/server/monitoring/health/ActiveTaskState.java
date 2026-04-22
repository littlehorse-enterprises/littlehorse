package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveTaskState that = (ActiveTaskState) o;
        return partition == that.partition && Objects.equals(processingLag, that.processingLag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partition, processingLag);
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
}
