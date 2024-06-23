package io.littlehorse.server.monitoring;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StandbyUpdateListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class InstanceStoreTest {

    private final StandbyStoresOnInstance instanceStore = new StandbyStoresOnInstance("my-store", 10);

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
    public void shouldReplaceMetric() {
        TopicPartition tp1 = new TopicPartition("changelo1", 0);
        TopicPartition tp2A = new TopicPartition("changelo1", 1);
        TopicPartition tp3 = new TopicPartition("changelo1", 2);
        TopicPartition tp2B = new TopicPartition("changelo1", 1);
        instanceStore.recordOffsets(tp1, 100, 1000);
        instanceStore.recordOffsets(tp2A, 1000, 1000);
        instanceStore.recordOffsets(tp3, 500, 1000);
        instanceStore.recordOffsets(tp2B, 300, 1000);
        Assertions.assertThat(instanceStore.totalLag()).isEqualTo(2100L);
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

    @Test
    void shouldSuspendPartition() {
        TopicPartition tp1 = new TopicPartition("changelo1", 0);
        TopicPartition tp2 = new TopicPartition("changelo1", 1);
        TopicPartition tp3 = new TopicPartition("changelo1", 2);
        instanceStore.recordOffsets(tp1, 100, 1000);
        instanceStore.recordOffsets(tp2, 1000, 1000);
        instanceStore.recordOffsets(tp3, 500, 1000);
        Assertions.assertThat(instanceStore.totalLag()).isEqualTo(1_400L);
        instanceStore.suspendPartition(tp3, 500, 1000, StandbyUpdateListener.SuspendReason.PROMOTED);
        Assertions.assertThat(instanceStore.totalLag()).isEqualTo(900L);
    }
}
