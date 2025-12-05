package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.TaskMetadata;

@Data
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
}
