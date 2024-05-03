package io.littlehorse.server.monitoring.health;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.monitoring.InProgressRestoration;
import io.littlehorse.server.monitoring.StandbyStoresOnInstance;
import io.littlehorse.server.monitoring.StandbyTopicPartitionMetrics;
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
    private long lag;
    private long changelogEndOffset;

    public StandbyTaskState() {}

    public StandbyTaskState(
            TaskMetadata meta,
            Map<TopicPartition, InProgressRestoration> restorations,
            LHServerConfig config,
            StandbyStoresOnInstance storeLagInfos) {

        StandbyTopicPartitionMetrics storeLagInfo = storeLagInfos.getPartitions().stream()
                .filter(lagInfo -> {
                    return lagInfo.getPartition()
                            == meta.topicPartitions().stream().findFirst().get().partition();
                })
                .findFirst()
                .get();

        Set<TopicPartition> topics = meta.topicPartitions();
        if (topics.size() != 1) {
            throw new IllegalStateException("Impossible. All LH processors have only one input topic");
        }

        TopicPartition tp = topics.stream().findFirst().get();

        this.topic = tp.topic();
        this.partition = tp.partition();
        this.processor = ServerHealthState.fromTopic(this.topic, config);
        this.changelogEndOffset = storeLagInfo.getEndOffset();
        this.lag = storeLagInfo.getCurrentLag();
    }
}
