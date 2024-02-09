package io.littlehorse.canary.aggregator.topology;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricsTopologyTest {
    private TopologyTestDriver testDriver;
    private TestInputTopic<BeatKey, Beat> inputTopic;
    private KeyValueStore<MetricKey, Double> store;

    private static Beat newTaskRunBeat() {
        return Beat.newBuilder()
                .setTime(Timestamps.now())
                .setTaskRunBeat(TaskRunBeat.newBuilder().setScheduledTime(Timestamps.now()))
                .build();
    }

    private static BeatKey newTaskRunBeatKey(
            String expectedHost, int expectedPort, String idempotencyKey, int attemptNumber) {
        return BeatKey.newBuilder()
                .setServerHost(expectedHost)
                .setServerPort(expectedPort)
                .setTaskRunBeatKey(TaskRunBeatKey.newBuilder()
                        .setIdempotencyKey(idempotencyKey)
                        .setAttemptNumber(attemptNumber))
                .build();
    }

    private static Beat newLatencyBeat(int latency) {
        return Beat.newBuilder()
                .setTime(Timestamps.now())
                .setLatencyBeat(LatencyBeat.newBuilder().setLatency(latency))
                .build();
    }

    private static BeatKey newLatencyBeatKey(String expectedHost, int expectedPort, String expectedMetricName) {
        return BeatKey.newBuilder()
                .setServerHost(expectedHost)
                .setServerPort(expectedPort)
                .setLatencyBeatKey(LatencyBeatKey.newBuilder().setName(expectedMetricName))
                .build();
    }

    private static MetricKey newMetricKey(String expectedHost, int expectedPort, String expectedMetricName) {
        return MetricKey.newBuilder()
                .setServerHost(expectedHost)
                .setServerPort(expectedPort)
                .setId(expectedMetricName)
                .build();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        String inputTopicName = "metrics";

        Consumed<MetricKey, Double> serdes = Consumed.with(ProtobufSerdes.MetricKey(), Serdes.Double());

        MetricsTopology metricsTopology =
                new MetricsTopology(inputTopicName, Duration.ofMinutes(2).toMillis());

        Properties properties = new Properties();
        properties.put(
                StreamsConfig.STATE_DIR_CONFIG,
                Files.createTempDirectory("canaryStreamUnitTest").toString());

        testDriver = new TopologyTestDriver(metricsTopology.toTopology(), properties);
        inputTopic = testDriver.createInputTopic(
                inputTopicName,
                ProtobufSerdes.BeatKey().serializer(),
                ProtobufSerdes.Beat().serializer());

        store = testDriver.getKeyValueStore(METRICS_STORE);
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void calculateLatency() {
        String metricName = "my_metric";
        String host = "localhost";
        int port = 2023;

        BeatKey beatKey = newLatencyBeatKey(host, port, metricName);

        List<Beat> beats = List.of(newLatencyBeat(20), newLatencyBeat(40), newLatencyBeat(10), newLatencyBeat(10));
        beats.forEach(metric -> inputTopic.pipeInput(beatKey, metric));

        assertThat(store.get(newMetricKey(host, port, metricName + "_avg"))).isEqualTo(20.);
        assertThat(store.get(newMetricKey(host, port, metricName + "_max"))).isEqualTo(40.);
    }

    @Test
    void countDuplicated() {
        String host = "localhost";
        int port = 2023;

        BeatKey beatKey = newTaskRunBeatKey(host, port, UUID.randomUUID().toString(), 0);

        List<Beat> beats = List.of(newTaskRunBeat(), newTaskRunBeat());
        beats.forEach(metric -> inputTopic.pipeInput(beatKey, metric));

        assertThat(store.get(newMetricKey(host, port, "duplicated_task_run_max_count")))
                .isEqualTo(1.);
    }

    @Test
    void countDuplicatedForTwoTasks() {
        String host = "localhost";
        int port = 2023;

        BeatKey beatKey1 = newTaskRunBeatKey(host, port, UUID.randomUUID().toString(), 0);
        BeatKey beatKey2 = newTaskRunBeatKey(host, port, UUID.randomUUID().toString(), 0);

        List<Beat> beats = List.of(newTaskRunBeat(), newTaskRunBeat());
        beats.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));
        beats.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));

        assertThat(store.get(newMetricKey(host, port, "duplicated_task_run_max_count")))
                .isEqualTo(2.);
    }

    @Test
    void countDuplicatedForTwoServers() {
        String host1 = "localhost";
        String host2 = "localhost2";
        int port1 = 2023;
        int port2 = 2024;

        BeatKey beatKey1 = newTaskRunBeatKey(host1, port1, UUID.randomUUID().toString(), 0);
        BeatKey beatKey2 = newTaskRunBeatKey(host2, port2, UUID.randomUUID().toString(), 0);

        List<Beat> beats = List.of(newTaskRunBeat(), newTaskRunBeat());
        beats.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));
        beats.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));

        assertThat(store.get(newMetricKey(host1, port1, "duplicated_task_run_max_count")))
                .isEqualTo(1.);
        assertThat(store.get(newMetricKey(host2, port2, "duplicated_task_run_max_count")))
                .isEqualTo(1.);
    }
}
