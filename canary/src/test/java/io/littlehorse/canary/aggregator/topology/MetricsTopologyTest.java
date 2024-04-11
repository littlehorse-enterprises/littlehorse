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
        return MetricValue.newBuilder().setValue(value).build();
    }

    private static MetricKey newMetricKey(String id) {
        return newMetricKey(HOST_1, PORT_1, id);
    }

    private static MetricKey newMetricKey(String id, String status) {
        return newMetricKey(HOST_1, PORT_1, id, status);
    }

    private static MetricKey newMetricKey(String host, int port, String id) {
        return newMetricKey(host, port, id, "ok");
    }

    private static MetricKey newMetricKey(String host, int port, String id, String status) {
        return MetricKey.newBuilder()
                .setServerHost(host)
                .setServerPort(port)
                .setId(id)
                .addTags(Tag.newBuilder().setKey("status").setValue(status).build())
                .build();
    }

    private static TestRecord<BeatKey, BeatValue> newBeat(BeatType type, String id, long latency) {
        return newBeat(HOST_1, PORT_1, type, id, latency, BeatStatus.OK);
    }

    private static TestRecord<BeatKey, BeatValue> newBeat(
            BeatType type, String id, long latency, BeatStatus beatStatus) {
        return newBeat(HOST_1, PORT_1, type, id, latency, beatStatus);
    }

    private static TestRecord<BeatKey, BeatValue> newBeat(
            String host, int port, BeatType type, String id, long latency, BeatStatus beatStatus) {
        BeatKey key = BeatKey.newBuilder()
                .setServerHost(host)
                .setServerPort(port)
                .setStatus(beatStatus)
                .setType(type)
                .setId(id)
                .build();
        BeatValue value = BeatValue.newBuilder()
                .setTime(Timestamps.now())
                .setLatency(latency)
                .build();
        return new TestRecord<>(key, value);
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

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30));

        assertThat(getCount()).isEqualTo(3);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void calculateCountAndLatencyForWfRunRequestForTwoStatus() {
        BeatType expectedType = BeatType.WF_RUN_REQUEST;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30));

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20, BeatStatus.ERROR));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10, BeatStatus.ERROR));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30, BeatStatus.ERROR));

        assertThat(getCount()).isEqualTo(6);

        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
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

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30));

        assertThat(getCount()).isEqualTo(3);
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_avg")))
                .isEqualTo(newMetricValue(20.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_max")))
                .isEqualTo(newMetricValue(30.));
        assertThat(store.get(newMetricKey("canary_" + expectedTypeName + "_count")))
                .isEqualTo(newMetricValue(3.));
    }

    @Test
    void calculateCountAndLatencyForTaskRunWithNoDuplicated() {
        BeatType expectedType = BeatType.TASK_RUN_EXECUTION;
        String expectedTypeName = expectedType.name().toLowerCase();

        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 20));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 10));
        inputTopic.pipeInput(newBeat(expectedType, getRandomId(), 30));

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

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 20));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 10));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 30));

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

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId1, 20));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId1, 10));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId1, 30));

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId2, 20));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId2, 30));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId2, 40));

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

        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 20));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 10));
        inputTopic.pipeInput(newBeat(expectedType, expectedUniqueId, 30));

        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 20, BeatStatus.OK));
        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 10, BeatStatus.OK));
        inputTopic.pipeInput(newBeat(HOST_2, PORT_2, expectedType, expectedUniqueId, 30, BeatStatus.OK));

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
