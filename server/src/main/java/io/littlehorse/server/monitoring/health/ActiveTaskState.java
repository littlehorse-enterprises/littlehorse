package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.InProgressRestoration;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.TaskMetadata;

@Data
public class ActiveTaskState {

    private LHProcessorType processor;
    private String topic;
    private int partition;

    private Long processingLag;

    private boolean isRestoring;
    private long restorationLag;

    public ActiveTaskState() {}

    public ActiveTaskState(
            TaskMetadata meta, Map<TopicPartition, InProgressRestoration> restorations, LHServerConfig config) {

        Set<TopicPartition> topics = meta.topicPartitions();
        if (topics.size() != 1) {
            throw new IllegalStateException("Impossible. All LH processors have only one input topic");
        }

        TopicPartition tp = topics.stream().findFirst().get();

        this.topic = tp.topic();
        this.partition = tp.partition();
        this.processor = ServerHealthState.fromTopic(this.topic, config);

        Long endOffset = meta.endOffsets().get(tp);
        Long committedOffset = meta.committedOffsets().get(tp);
        this.processingLag = (endOffset == null || committedOffset == null) ? null : endOffset - committedOffset;

        this.isRestoring = restorations.containsKey(tp);
        if (this.isRestoring) {
            this.restorationLag = restorations.get(tp).getRemaining();
        }
    }
}
