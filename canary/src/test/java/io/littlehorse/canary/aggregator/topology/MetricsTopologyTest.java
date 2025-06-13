package io.littlehorse.canary.aggregator.topology;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Streams;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.proto.MetricValue;
import io.littlehorse.canary.proto.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.TestRecord;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricsTopologyTest {

    public static final String HOST_1 = "localhost";
    public static final int PORT_1 = 2023;

    public static final String HOST_2 = "localhost2";
    public static final int PORT_2 = 2024;

    private TopologyTestDriver testDriver;
    private TestInputTopic<BeatKey, BeatValue> inputTopic;
    private KeyValueStore<MetricKey, MetricValue> store;

    private static String getRandomId() {
        return UUID.randomUUID().toString();
    }

    private MetricValue newMetricValue(double count) {
        return MetricValue.newBuilder().putValues("count", count).build();
    }

    private MetricValue newMetricValue(double avg, double max, double count) {
        return MetricValue.newBuilder()
                .putValues("avg", avg)
                .putValues("max", max)
                .putValues("count", count)
                .build();
    }

    private MetricKey newMetricKey(String id) {
        return newMetricKey(HOST_1, PORT_1, id);
    }

    private MetricKey newMetricKey(String id, String status) {
        return newMetricKey(HOST_1, PORT_1, id, status, null);
    }

    private MetricKey newMetricKey(String host, int port, String id) {
        return newMetricKey(host, port, id, null, null);
    }

    private MetricKey newMetricKey(String id, String status, Map<String, String> tags) {
        return newMetricKey(HOST_1, PORT_1, id, status, tags);
    }

    private MetricKey newMetricKey(String host, int port, String id, String status, Map<String, String> tags) {
        MetricKey.Builder builder =
                MetricKey.newBuilder().setServerHost(host).setServerPort(port).setName(id);

        if (status != null) {
            builder.addTags(Tag.newBuilder().setKey("status").setValue(status).build());
        }

        if (tags != null) {
            List<Tag> tagList = tags.entrySet().stream()
                    .map(entry -> Tag.newBuilder()
                            .setKey(entry.getKey())
                            .setValue(entry.getValue())
                            .build())
                    .toList();
            builder.addAllTags(tagList);
        }

        return builder.build();
    }

    private TestRecord<BeatKey, BeatValue> newBeat(BeatType type, String id, Long latency) {
        return newBeat(HOST_1, PORT_1, type, id, latency, null, null);
    }

    private TestRecord<BeatKey, BeatValue> newBeat(BeatType type, String id, Long latency, String beatStatus) {
        return newBeat(HOST_1, PORT_1, type, id, latency, beatStatus, null);
    }

    private TestRecord<BeatKey, BeatValue> newBeat(
            BeatType type, String id, Long latency, String beatStatus, Map<String, String> tags) {
        return newBeat(HOST_1, PORT_1, type, id, latency, beatStatus, tags);
    }

    private TestRecord<BeatKey, BeatValue> newBeat(
            String host,
            int port,
            BeatType type,
            String id,
            Long latency,
            String beatStatus,
            Map<String, String> tags) {

        BeatKey.Builder keyBuilder = BeatKey.newBuilder()
                .setServerHost(host)
                .setServerPort(port)
                .setType(type)
                .setId(id);

        BeatValue.Builder valueBuilder =
                // set time to 0 (Timestamps.EPOCH) to prevent windows to be closed
                BeatValue.newBuilder().setTime(Timestamps.EPOCH);

        if (beatStatus != null) {
            keyBuilder.addTags(
                    Tag.newBuilder().setKey("status").setValue(beatStatus).build());
        }

        if (tags != null) {
            List<Tag> tagList = tags.entrySet().stream()
                    .map(entry -> Tag.newBuilder()
                            .setKey(entry.getKey())
                            .setValue(entry.getValue())
                            .build())
                    .toList();
            keyBuilder.addAllTags(tagList);
        }

        if (latency != null) {
            valueBuilder.setLatency(latency);
        }

        return new TestRecord<>(keyBuilder.build(), valueBuilder.build());
    }

    @BeforeEach
    void beforeEach() throws IOException {
        String inputTopicName = "metrics";

        MetricsTopology metricsTopology = new MetricsTopology(inputTopicName, Duration.ofMinutes(2));

        Properties properties = new Properties();
        properties.put(
                StreamsConfig.STATE_DIR_CONFIG,
                Files.createTempDirectory("canaryStreamUnitTest").toString());

        // set time to 0 (Instant.EPOCH) to prevent windows to be closed
        testDriver = new TopologyTestDriver(metricsTopology.toTopology(), properties, Instant.EPOCH);
        inputTopic = testDriver.createInputTopic(
                inputTopicName,
                ProtobufSerdes.BeatKey().serializer(),
                ProtobufSerdes.BeatValue().serializer());

        store = testDriver.getKeyValueStore(METRICS_STORE);
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        store.close();
    }

    @Test
    void shouldCalculateCountAndLatencyForWfRunRequest() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "ok"));

        assertThat(getCount()).isEqualTo(1);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "ok"))).isEqualTo(newMetricValue(20., 30., 3.));
    }

    @Test
    void shouldIncludeBeatTagsIntoMetrics() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();
        Map<String, String> expectedTags = Map.of("my_tag", "value");

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok", expectedTags));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok"));

        assertThat(getCount()).isEqualTo(2);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "ok", expectedTags)))
                .isEqualTo(newMetricValue(20., 20., 1.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "ok"))).isEqualTo(newMetricValue(20., 20., 1.));
    }

    @Test
    void shouldCalculateCountForExhaustedRetries() {
        BeatType expectedType = BeatType.GET_WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();
        Map<String, String> reason = Map.of("reason", "canary_exhausted_retries");

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), null, "error", reason));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), null, "error", reason));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), null, "error", reason));

        assertThat(getCount()).isEqualTo(1);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "error", reason)))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void shouldCalculateCountAndLatencyForWfRunRequestForTwoStatus() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "ok"));

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "error"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "error"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "error"));

        assertThat(getCount()).isEqualTo(2);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "ok"))).isEqualTo(newMetricValue(20., 30., 3.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "error")))
                .isEqualTo(newMetricValue(20., 30., 3.));
    }

    @Test
    void shouldCalculateCountAndLatencyForGetWfRunRequest() {
        BeatType expectedType = BeatType.GET_WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "completed"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "completed"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "completed"));

        assertThat(getCount()).isEqualTo(1);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "completed")))
                .isEqualTo(newMetricValue(20., 30., 3.));
    }

    @Test
    void shouldCalculateCountAndLatencyForTaskRunWithNoDuplicated() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L));

        assertThat(getCount()).isEqualTo(1);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName))).isEqualTo(newMetricValue(20., 30., 3.));
    }

    @Test
    void shouldCalculateCountAndLatencyForTaskRunWithDuplicated() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();
        String expectedUniqueId = getRandomId();

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 20L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 10L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 30L));

        assertThat(getCount()).isEqualTo(2);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName))).isEqualTo(newMetricValue(20., 30., 3.));
        assertThat(store.get(newMetricKey("canary_duplicated_task_run"))).isEqualTo(newMetricValue(1.));
    }

    @Test
    void shouldCalculateCountAndLatencyForTaskRunWithTwoDuplicated() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();
        String expectedUniqueId1 = getRandomId();
        String expectedUniqueId2 = getRandomId();

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId1, 20L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId1, 10L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId1, 30L));

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId2, 20L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId2, 30L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId2, 40L));

        assertThat(getCount()).isEqualTo(2);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName))).isEqualTo(newMetricValue(25., 40., 6.));
        assertThat(store.get(newMetricKey("canary_duplicated_task_run"))).isEqualTo(newMetricValue(2.));
    }

    @Test
    void shouldCalculateCountAndLatencyForTaskRunWithDuplicatedAndTwoServers() throws InterruptedException {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();
        String expectedUniqueId = getRandomId();

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 20L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 10L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 30L));

        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 20L, null, null));
        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 10L, null, null));
        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 30L, null, null));

        assertThat(getCount()).isEqualTo(4);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName))).isEqualTo(newMetricValue(20., 30., 3.));
        assertThat(store.get(newMetricKey("canary_duplicated_task_run"))).isEqualTo(newMetricValue(1.));
        assertThat(store.get(newMetricKey(HOST_2, PORT_2, "canary_" + expectedTypeName)))
                .isEqualTo(newMetricValue(20., 30., 3.));
        assertThat(store.get(newMetricKey(HOST_2, PORT_2, "canary_duplicated_task_run")))
                .isEqualTo(newMetricValue(1.));
    }

    public void await(ThrowingRunnable runnable) {
        Awaitility.with()
                .pollInterval(Duration.ofSeconds(1))
                .await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(runnable);
    }

    private long getCount() {
        return Streams.stream(store.all()).count();
    }
}
