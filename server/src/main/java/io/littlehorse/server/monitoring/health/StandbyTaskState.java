package io.littlehorse.server.monitoring.health;

import io.littlehorse.server.monitoring.StandbyTopicPartitionMetrics;
import java.util.Set;
import lombok.Getter;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.TaskMetadata;

@Getter
public class StandbyTaskState {

    private int partition;
    private long lag;
    private long changelogEndOffset;

    public StandbyTaskState() {}

    public StandbyTaskState(TaskMetadata meta, StandbyTopicPartitionMetrics storeLagInfo) {
        Set<TopicPartition> topics = meta.topicPartitions();

        TopicPartition tp = topics.stream().findFirst().get();

        this.partition = tp.partition();
        this.changelogEndOffset = storeLagInfo.getEndOffset();
        this.lag = storeLagInfo.getCurrentLag();
    }
}
