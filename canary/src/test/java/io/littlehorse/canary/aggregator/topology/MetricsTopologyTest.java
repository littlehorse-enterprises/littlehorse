package io.littlehorse.canary.aggregator.topology;

import static io.littlehorse.canary.aggregator.topology.MetricsTopology.METRICS_STORE;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Streams;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.TestRecord;
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
    private static MetricValue newMetricValue(double value) {
        return MetricValue.newBuilder().putValues("count", value).build();
    }
    private static MetricValue newMetricValueCount(double value) {
        return MetricValue.newBuilder().putValues("count", value).build();
    }

    private static MetricValue newMetricValueAvg(double avg, double max) {
        return MetricValue.newBuilder().putValues("avg", avg).putValues("max", max).build();
    }

    private static MetricKey newMetricKey(String id) {
        return newMetricKey(HOST_1, PORT_1, id);
    }

    private static MetricKey newMetricKey(String id, String status) {
        return newMetricKey(HOST_1, PORT_1, id, status, null);
    }

    private static MetricKey newMetricKey(String host, int port, String id) {
        return newMetricKey(host, port, id, null, null);
    }

    private static MetricKey newMetricKey(String id, String status, Map<String, String> tags) {
        return newMetricKey(HOST_1, PORT_1, id, status, tags);
    }

    private static MetricKey newMetricKey(String host, int port, String id, String status, Map<String, String> tags) {
        MetricKey.Builder builder =
                MetricKey.newBuilder().setServerHost(host).setServerPort(port).setId(id);

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

    private static TestRecord<BeatKey, BeatValue> newBeat(BeatType type, String id, Long latency) {
        return newBeat(HOST_1, PORT_1, type, id, latency, null, null);
    }

    private static TestRecord<BeatKey, BeatValue> newBeat(BeatType type, String id, Long latency, String beatStatus) {
        return newBeat(HOST_1, PORT_1, type, id, latency, beatStatus, null);
    }

    private static TestRecord<BeatKey, BeatValue> newBeat(
            BeatType type, String id, Long latency, String beatStatus, Map<String, String> tags) {
        return newBeat(HOST_1, PORT_1, type, id, latency, beatStatus, tags);
    }

    private static TestRecord<BeatKey, BeatValue> newBeat(
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
        BeatValue.Builder valueBuilder = BeatValue.newBuilder().setTime(Timestamps.now());

        if (beatStatus != null) {
            keyBuilder.setStatus(beatStatus);
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
        store.close();
    }

    @Test
    void calculateCountAndLatencyForWfRunRequest() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "ok"));

        assertThat(getCount()).isEqualTo(1);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "ok")))
                .isEqualTo(newMetricValueAvg(20., 30.));
//        assertThat(store.get(newMetricKey("canary_" + expectedTypeName, "ok")))
//                .isEqualTo(newMetricValueCount(3.));
    }

    @Test
    void includeBeatTagsIntoMetrics() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();
        Map<String, String> expectedTags = Map.of("my_tag", "value");

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok", expectedTags));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok"));

        assertThat(getCount()).isEqualTo(6);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg", "ok", expectedTags)))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max", "ok", expectedTags)))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count", "ok", expectedTags)))
                .isEqualTo(newMetricValue(1.));

        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg", "ok")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max", "ok")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count", "ok")))
                .isEqualTo(newMetricValue(1.));
    }

    @Test
    void calculateCountForExhaustedRetries() {
        BeatType expectedType = BeatType.GET_WF_RUN_EXHAUSTED_RETRIES;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), null));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), null));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), null));

        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void calculateCountAndLatencyForWfRunRequestForTwoStatus() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "ok"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "ok"));

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "error"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "error"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "error"));

        assertThat(getCount()).isEqualTo(6);

        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg", "ok")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max", "ok")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count", "ok")))
                .isEqualTo(newMetricValue(3.));

        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg", "error")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max", "error")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count", "error")))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void calculateCountAndLatencyForGetWfRunRequest() {
        BeatType expectedType = BeatType.GET_WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L, "completed"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L, "completed"));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L, "completed"));

        assertThat(getCount()).isEqualTo(3);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg", "completed")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max", "completed")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count", "completed")))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void calculateCountAndLatencyForTaskRunWithNoDuplicated() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20L));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10L));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30L));

        assertThat(getCount()).isEqualTo(3);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void calculateCountAndLatencyForTaskRunWithDuplicated() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();
        String expectedUniqueId = getRandomId();

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 20L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 10L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 30L));

        assertThat(getCount()).isEqualTo(4);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
        assertThat(store.get(newMetricKey("canary_duplicated_task_run_count"))).isEqualTo(newMetricValue(1.));
    }

    @Test
    void calculateCountAndLatencyForTaskRunWithTwoDuplicated() {
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

        assertThat(getCount()).isEqualTo(4);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(25.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(40.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(6.));
        assertThat(store.get(newMetricKey("canary_duplicated_task_run_count"))).isEqualTo(newMetricValue(2.));
    }

    @Test
    void calculateCountAndLatencyForTaskRunWithDuplicatedAndTwoServers() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();
        String expectedUniqueId = getRandomId();

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 20L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 10L));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 30L));

        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 20L, null, null));
        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 10L, null, null));
        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 30L, null, null));

        assertThat(getCount()).isEqualTo(8);

        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
        assertThat(store.get(newMetricKey("canary_duplicated_task_run_count"))).isEqualTo(newMetricValue(1.));

        assertThat(store.get(newMetricKey(HOST_2, PORT_2, "canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey(HOST_2, PORT_2, "canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey(HOST_2, PORT_2, "canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
        assertThat(store.get(newMetricKey(HOST_2, PORT_2, "canary_duplicated_task_run_count")))
                .isEqualTo(newMetricValue(1.));
    }

    private long getCount() {
        return Streams.stream(store.all()).count();
    }
}
