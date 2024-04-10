package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.MetricKey;
import java.time.Duration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class MetricsTopology {

    private static final Consumed<BeatKey, BeatValue> BEATS_SERDES = Consumed.with(
                    ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue())
            .withTimestampExtractor(new BeatTimeExtractor());

    public static final String METRICS_STORE = "metrics";
    private final StreamsBuilder streamsBuilder;

    public MetricsTopology(final String inputTopic, final long storeRetention) {
        streamsBuilder = new StreamsBuilder();

        final KStream<BeatKey, BeatValue> beatsStream = streamsBuilder.stream(inputTopic, BEATS_SERDES);

        final LatencyTopology latencyTopology = new LatencyTopology(
                beatsStream,
                TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)),
                Duration.ofMillis(storeRetention));

        final DuplicatedTaskRunTopology taskRunTopology =
                new DuplicatedTaskRunTopology(beatsStream, Duration.ofMillis(storeRetention));

        latencyTopology
                .getStream()
                .merge(taskRunTopology.getStream())
                .toTable(
                        Named.as(METRICS_STORE),
                        Materialized.<MetricKey, Double, KeyValueStore<Bytes, byte[]>>as(METRICS_STORE)
                                .withKeySerde(ProtobufSerdes.MetricKey())
                                .withValueSerde(Serdes.Double())
                                .withRetention(Duration.ofMillis(storeRetention)));
    }

    public Topology toTopology() {
        return streamsBuilder.build();
    }
}
