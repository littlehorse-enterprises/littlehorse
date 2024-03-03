package io.littlehorse.server.monitoring;


import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TopicPartitionMetricsTest {

    private TopicPartition tp = Mockito.mock();

    @Test
    public void shouldCalculateLagFromOffsets() {
        long currentLag = new TopicPartitionMetrics(tp, 100, 1000).getCurrentLag();
        Assertions.assertThat(currentLag).isEqualTo(900);
    }

    @Test
    public void shouldSetDefaultLag() {
        long currentLag = new TopicPartitionMetrics(tp, 1000, 100).getCurrentLag();
        Assertions.assertThat(currentLag).isEqualTo(-1);
    }

}