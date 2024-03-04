package io.littlehorse.server.monitoring;

import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class InstanceStoreTest {

    private final InstanceStore instanceStore = new InstanceStore("my-store", 10);

    @Test
    void shouldCalculateOverallLag() {
        TopicPartition tp1 = new TopicPartition("changelo1", 0);
        TopicPartition tp2 = new TopicPartition("changelo1", 1);
        TopicPartition tp3 = new TopicPartition("changelo1", 2);
        instanceStore.recordOffsets(tp1, 100, 1000);
        instanceStore.recordOffsets(tp2, 1000, 1000);
        instanceStore.recordOffsets(tp3, 500, 1000);
        Assertions.assertThat(instanceStore.totalLag()).isEqualTo(1_400L);
    }

    @Test
    void shouldIgnoreDefaultLag() {
        TopicPartition tp1 = new TopicPartition("changelo1", 0);
        TopicPartition tp2 = new TopicPartition("changelo1", 1);
        TopicPartition tp3 = new TopicPartition("changelo1", 2);
        instanceStore.recordOffsets(tp1, 100, -1);
        instanceStore.recordOffsets(tp2, 1000, 1000);
        instanceStore.recordOffsets(tp3, 500, 1000);
        Assertions.assertThat(instanceStore.totalLag()).isEqualTo(500L);
    }
}
