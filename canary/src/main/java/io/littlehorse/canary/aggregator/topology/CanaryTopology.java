package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.internal.EventTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;

import java.time.Duration;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

public class CanaryTopology {

    private static final Consumed<BeatKey, BeatValue> BEATS_SERDES = Consumed.with(
                    ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue())
            .withTimestampExtractor(new BeatTimeExtractor());

    private static final Consumed<EventKey, EventValue> EVENTS_SERDES = Consumed.with(
                    ProtobufSerdes.EventKey(), ProtobufSerdes.EventValue())
            .withTimestampExtractor(new EventTimeExtractor());

    public static final String METRICS_STORE = "metrics";
    private final StreamsBuilder streamsBuilder;

    public CanaryTopology(final String eventsTopic, String beatsTopic, final long storeRetention) {
        streamsBuilder = new StreamsBuilder();

        final KStream<EventKey, EventValue> eventsStream = streamsBuilder.stream(eventsTopic, EVENTS_SERDES);
        eventsStream

        final KStream<BeatKey, BeatValue> beatsStream = streamsBuilder.stream(beatsTopic, BEATS_SERDES);

//        final LatencySubTopology latencySubTopology = new LatencySubTopology(
//                beatsStream,
//                TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(30)),
//                Duration.ofMillis(storeRetention));
//
//        final EventSubTopology eventSubTopology =
//                new EventSubTopology(beatsStream, Duration.ofMillis(storeRetention), eventsTopic);
//
//        eventSubTopology
//                .getStream()
//                .merge(latencySubTopology.getStream())
//                .toTable(
//                        Named.as(METRICS_STORE),
//                        Materialized.<MetricKey, MetricValue, KeyValueStore<Bytes, byte[]>>as(METRICS_STORE)
//                                .withKeySerde(ProtobufSerdes.MetricKey())
//                                .withValueSerde(ProtobufSerdes.MetricValue())
//                                .withRetention(Duration.ofMillis(storeRetention)));
    }

    public Topology toTopology() {
        return streamsBuilder.build();
    }
}
