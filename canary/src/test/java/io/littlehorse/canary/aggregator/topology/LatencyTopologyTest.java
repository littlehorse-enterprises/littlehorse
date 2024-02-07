package io.littlehorse.canary.aggregator.topology;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.aggregator.internal.MetricTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.Latency;
import io.littlehorse.canary.proto.Metadata;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.MetricAverage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LatencyTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Metric> inputTopic;

    @BeforeEach
    void beforeEach() throws IOException {
        String topicName = "canary-metric-beats";

        Consumed<String, Metric> serdes = Consumed.with(Serdes.String(), ProtobufSerdes.Metric())
                .withTimestampExtractor(new MetricTimeExtractor());

        StreamsBuilder builder = new StreamsBuilder();
        new LatencyTopology(builder.stream(topicName, serdes));
        Topology topology = builder.build();

        Properties properties = new Properties();
        properties.put(
                StreamsConfig.STATE_DIR_CONFIG,
                Files.createTempDirectory("canaryStreamUnitTest").toString());

        testDriver = new TopologyTestDriver(topology, properties);
        inputTopic = testDriver.createInputTopic(
                topicName, Serdes.String().serializer(), ProtobufSerdes.Metric().serializer());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void calculateLatency() {
        String key = "localhost:2023";
        Metric metric1 = buildLatencyMetric(20);
        Metric metric2 = buildLatencyMetric(40);

        inputTopic.pipeInput(key, metric1);
        inputTopic.pipeInput(key, metric2);

        KeyValueStore store = testDriver.getKeyValueStore("latency-metric");

        assertThat(store.get(key))
                .isEqualTo(MetricAverage.newBuilder()
                        .setSum(60)
                        .setAvg(30)
                        .setCount(2)
                        .setPeak(40)
                        .build());
    }

    private static Metric buildLatencyMetric(int latency) {
        return Metric.newBuilder()
                .setMetadata(Metadata.newBuilder().setTime(Timestamps.now()))
                .setLatency(Latency.newBuilder().setLatency(latency))
                .build();
    }
}
