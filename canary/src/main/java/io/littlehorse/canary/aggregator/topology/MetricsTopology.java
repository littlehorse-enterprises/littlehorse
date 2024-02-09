package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.MetricKey;
import java.time.Duration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;

public class MetricsTopology {

    private static final Consumed<BeatKey, Beat> BEATS_SERDES = Consumed.with(
                    ProtobufSerdes.BeatKey(), ProtobufSerdes.Beat())
            .withTimestampExtractor(new BeatTimeExtractor());

    public static final String METRICS_STORE = "metrics";
    private final StreamsBuilder streamsBuilder;

    public MetricsTopology(final String inputTopic, final long storeRetention) {
        streamsBuilder = new StreamsBuilder();

        final KStream<BeatKey, Beat> beatsStream = streamsBuilder.stream(inputTopic, BEATS_SERDES);

        final LatencyTopology latencyTopology = new LatencyTopology(
                beatsStream,
                TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)),
                Duration.ofMillis(storeRetention));
        final DuplicatedTaskRunTopology taskRunTopology =
                new DuplicatedTaskRunTopology(beatsStream, Duration.ofMillis(storeRetention));

        latencyTopology
                .getStream()
                .merge(taskRunTopology.getStream())
                .toTable(Materialized.<MetricKey, Double, KeyValueStore<Bytes, byte[]>>as(METRICS_STORE)
                        .withKeySerde(ProtobufSerdes.MetricKey())
                        .withValueSerde(Serdes.Double())
                        .withRetention(Duration.ofMillis(storeRetention)));
    }

    public Topology toTopology() {
        return streamsBuilder.build();
    }
}
