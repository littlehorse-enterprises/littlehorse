package io.littlehorse.canary.aggregator.topology;

import static io.littlehorse.canary.aggregator.topology.DuplicatedTaskRunTopology.DUPLICATED_TASK_RUN;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DuplicatedTaskRunTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<BeatKey, Beat> inputTopic;

    private static Beat newEmptyBeat() {
        return Beat.newBuilder()
                .setTime(Timestamps.now())
                .setTaskRunBeat(TaskRunBeat.newBuilder().setScheduledTime(Timestamps.now()))
                .build();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        String topicName = "canary-metric-beats";

        Consumed<BeatKey, Beat> serdes = Consumed.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.Beat())
                .withTimestampExtractor(new BeatTimeExtractor());

        StreamsBuilder builder = new StreamsBuilder();
        new DuplicatedTaskRunTopology(builder.stream(topicName, serdes));
        Topology topology = builder.build();

        Properties properties = new Properties();
        properties.put(
                StreamsConfig.STATE_DIR_CONFIG,
                Files.createTempDirectory("canaryStreamUnitTest").toString());

        testDriver = new TopologyTestDriver(topology, properties);
        inputTopic = testDriver.createInputTopic(
                topicName,
                ProtobufSerdes.BeatKey().serializer(),
                ProtobufSerdes.Beat().serializer());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void countDuplicated() {
        String expectedMetricName = "my_metric";
        String expectedHost = "localhost";
        int expectedPort = 2023;

        BeatKey key = BeatKey.newBuilder()
                .setServerHost(expectedHost)
                .setServerPort(expectedPort)
                .setTaskRunBeatKey(TaskRunBeatKey.newBuilder()
                        .setIdempotencyKey("my-unique-key")
                        .setAttemptNumber(0))
                .build();

        List<Beat> beats = List.of(newEmptyBeat(), newEmptyBeat(), newEmptyBeat(), newEmptyBeat());

        beats.forEach(metric -> inputTopic.pipeInput(key, metric));
        KeyValueStore store = testDriver.getKeyValueStore(DUPLICATED_TASK_RUN);

        assertThat(store.get(MetricKey.newBuilder()
                        .setServerHost(expectedHost)
                        .setServerPort(expectedPort)
                        .setId("duplicated_task_run_count")
                        .build()))
                .isEqualTo(9);
    }
}
