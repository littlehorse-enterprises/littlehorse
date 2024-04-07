package io.littlehorse.canary.aggregator.topology;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;
import static io.littlehorse.canary.proto.MetricFactory.buildKey;
import static io.littlehorse.canary.proto.MetricFactory.buildValue;
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
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MetricsTopologyTest {
    public static final String EXPECTED_HOST = "localhost";
    public static final int EXPECTED_PORT = 2023;
    public static final String EXPECTED_VERSION = "1.0.0";
    private TopologyTestDriver testDriver;
    private TestInputTopic<BeatKey, BeatValue> inputTopic;
    private KeyValueStore<MetricKey, MetricValue> store;

    private static BeatValue newTaskRunBeat(long latency) {
        return BeatValue.newBuilder()
                .setTime(Timestamps.now())
                .setTaskRunBeat(TaskRunBeat.newBuilder()
                        .setScheduledTime(Timestamps.now())
                        .setLatency(latency))
                .build();
    }

    private static BeatKey newTaskRunBeatKey(
            String expectedHost, int expectedPort, String idempotencyKey, int attemptNumber) {
        return BeatKey.newBuilder()
                .setServerHost(expectedHost)
                .setServerPort(expectedPort)
                .setServerVersion(EXPECTED_VERSION)
                .setTaskRunBeatKey(TaskRunBeatKey.newBuilder()
                        .setIdempotencyKey(idempotencyKey)
                        .setAttemptNumber(attemptNumber))
                .build();
    }

    private static BeatValue newLatencyBeat(int latency) {
        return BeatValue.newBuilder()
                .setTime(Timestamps.now())
                .setLatencyBeat(LatencyBeat.newBuilder().setLatency(latency))
                .build();
    }

    private static BeatKey newLatencyBeatKey(String expectedHost, int expectedPort, String expectedMetricName) {
        return BeatKey.newBuilder()
                .setServerHost(expectedHost)
                .setServerPort(expectedPort)
                .setServerVersion(EXPECTED_VERSION)
                .setLatencyBeatKey(LatencyBeatKey.newBuilder().setId(expectedMetricName))
                .build();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        String inputTopicName = "metrics";

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
                ProtobufSerdes.BeatValue().serializer());

        store = testDriver.getKeyValueStore(METRICS_STORE);
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Nested
    class Latency {

        @Test
        void calculateLatency() {
            String metricName = "my_metric";

            BeatKey beatKey = newLatencyBeatKey(EXPECTED_HOST, EXPECTED_PORT, metricName);

            List<BeatValue> beats =
                    List.of(newLatencyBeat(20), newLatencyBeat(40), newLatencyBeat(10), newLatencyBeat(10));
            beats.forEach(metric -> inputTopic.pipeInput(beatKey, metric));

            assertThat(store.get(buildKey(metricName + "_avg", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(20).build());
            assertThat(store.get(buildKey(metricName + "_max", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(40).build());
        }

        @Test
        void calculateLatencyForDifferentHosts() {
            String metricName = "my_metric";

            String localhost1 = "localhost1";
            BeatKey beatKey1 = newLatencyBeatKey(localhost1, EXPECTED_PORT, metricName);
            List<BeatValue> beats1 =
                    List.of(newLatencyBeat(20), newLatencyBeat(40), newLatencyBeat(10), newLatencyBeat(10));
            beats1.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));

            String localhost2 = "localhost2";
            BeatKey beatKey2 = newLatencyBeatKey(localhost2, EXPECTED_PORT, metricName);
            List<BeatValue> beats2 =
                    List.of(newLatencyBeat(20), newLatencyBeat(40), newLatencyBeat(20), newLatencyBeat(10));
            beats2.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));

            assertThat(store.get(buildKey(metricName + "_avg", localhost1, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(20).build());
            assertThat(store.get(buildKey(metricName + "_max", localhost1, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(40).build());

            assertThat(store.get(buildKey(metricName + "_avg", localhost2, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(22.5).build());
            assertThat(store.get(buildKey(metricName + "_max", localhost2, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(40).build());
        }
    }

    @Nested
    class TaskRun {
        @Test
        void countDuplicated() {
            BeatKey beatKey = newTaskRunBeatKey(
                    EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0);

            List<BeatValue> beats = List.of(newTaskRunBeat(2), newTaskRunBeat(2));
            beats.forEach(metric -> inputTopic.pipeInput(beatKey, metric));

            assertThat(store.get(
                            buildKey("duplicated_task_run_max_count", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(1).build());
        }

        @Test
        void notDuplicatedWithDifferentAttempts() {
            String idempotencyKey = UUID.randomUUID().toString();

            inputTopic.pipeInput(newTaskRunBeatKey(EXPECTED_HOST, EXPECTED_PORT, idempotencyKey, 0), newTaskRunBeat(2));
            inputTopic.pipeInput(newTaskRunBeatKey(EXPECTED_HOST, EXPECTED_PORT, idempotencyKey, 1), newTaskRunBeat(2));
            inputTopic.pipeInput(newTaskRunBeatKey(EXPECTED_HOST, EXPECTED_PORT, idempotencyKey, 2), newTaskRunBeat(2));

            assertThat(store.get(
                            buildKey("duplicated_task_run_max_count", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isNull();
        }

        @Test
        void notDuplicated() {
            inputTopic.pipeInput(
                    newTaskRunBeatKey(
                            EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0),
                    newTaskRunBeat(2));
            inputTopic.pipeInput(
                    newTaskRunBeatKey(
                            EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0),
                    newTaskRunBeat(2));
            inputTopic.pipeInput(
                    newTaskRunBeatKey(
                            EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0),
                    newTaskRunBeat(2));

            assertThat(store.get(
                            buildKey("duplicated_task_run_max_count", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isNull();
        }

        @Test
        void countDuplicatedForTwoTasks() {
            BeatKey beatKey1 = newTaskRunBeatKey(
                    EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0);
            BeatKey beatKey2 = newTaskRunBeatKey(
                    EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0);

            List<BeatValue> beats = List.of(newTaskRunBeat(2), newTaskRunBeat(2));
            beats.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));
            beats.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));

            assertThat(store.get(
                            buildKey("duplicated_task_run_max_count", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(2).build());
        }

        @Test
        void countDuplicatedForTwoServers() {
            String host1 = "localhost";
            String host2 = "localhost2";
            int port1 = 2023;
            int port2 = 2024;

            BeatKey beatKey1 = newTaskRunBeatKey(host1, port1, UUID.randomUUID().toString(), 0);
            BeatKey beatKey2 = newTaskRunBeatKey(host2, port2, UUID.randomUUID().toString(), 0);

            List<BeatValue> beats = List.of(newTaskRunBeat(2), newTaskRunBeat(2));
            beats.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));
            beats.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));

            assertThat(store.get(buildKey("duplicated_task_run_max_count", host1, port1, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(1).build());
            assertThat(store.get(buildKey("duplicated_task_run_max_count", host2, port2, EXPECTED_VERSION)))
                    .isEqualTo(MetricValue.newBuilder().setValue(1).build());
        }

        @Test
        void calculateLatency() {
            inputTopic.pipeInput(
                    newTaskRunBeatKey(
                            EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0),
                    newTaskRunBeat(3));
            inputTopic.pipeInput(
                    newTaskRunBeatKey(
                            EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0),
                    newTaskRunBeat(2));
            inputTopic.pipeInput(
                    newTaskRunBeatKey(
                            EXPECTED_HOST, EXPECTED_PORT, UUID.randomUUID().toString(), 0),
                    newTaskRunBeat(4));

            assertThat(store.get(buildKey("task_run_latency_avg", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(buildValue(3));

            assertThat(store.get(buildKey("task_run_latency_max", EXPECTED_HOST, EXPECTED_PORT, EXPECTED_VERSION)))
                    .isEqualTo(buildValue(4));
        }
    }
}
