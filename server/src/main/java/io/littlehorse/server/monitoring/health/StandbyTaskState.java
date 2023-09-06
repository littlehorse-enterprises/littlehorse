package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.InProgressRestoration;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.TaskMetadata;

@Data
public class StandbyTaskState {

    private LHProcessorType processor;
    private String topic;
    private int partition;

    public StandbyTaskState() {}

    public StandbyTaskState(
            TaskMetadata meta, Map<TopicPartition, InProgressRestoration> restorations, LHServerConfig config) {

        Set<TopicPartition> topics = meta.topicPartitions();
        if (topics.size() != 1) {
            throw new IllegalStateException("Impossible. All LH processors have only one input topic");
        }

        TopicPartition tp = topics.stream().findFirst().get();

        this.topic = tp.topic();
        this.partition = tp.partition();
        this.processor = ServerHealthState.fromTopic(this.topic, config);

        // Note: Kafka Streams (accidentally) provides no way to calculate lag for
        // standby tasks. Future versions of this class will include such calculations.
    }
}
