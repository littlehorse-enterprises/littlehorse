package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

@Slf4j
public class MetricsTopology {

    public static final String METRICS_STORE = "metrics";
    public static final String LATENCY_AVG_STORE = "latency-avg";

    public static final Consumed<BeatKey, BeatValue> BEATS_SERDES = Consumed.with(
                    ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue())
            .withTimestampExtractor(new BeatTimeExtractor());

    private final String inputTopic;
    private final Duration storeRetention;

    public MetricsTopology(final String inputTopic, final Duration storeRetention) {
        this.inputTopic = inputTopic;
        this.storeRetention = storeRetention;
    }

    public Topology toTopology() {
        // initialize stream
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<BeatKey, BeatValue> beatsStream = streamsBuilder.stream(inputTopic, BEATS_SERDES);

        // calculate latency by beat type (removing id)
        beatsStream
                .groupBy(
                        (key, value) -> cleanBeatKeyId(key),
                        Grouped.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue()))
                //// reset aggregator every minute
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                //// calculate average
                .aggregate(
                        MetricsTopology::initializeAverageAggregator,
                        MetricsTopology::aggregateAverage,
                        initializeLatencyStore())
                .toStream((keyWindowed, value) -> keyWindowed.key())
                .flatMap(MetricsTopology::makeLatencyMetrics)
                //// peek
                .peek(MetricsTopology::peekMetrics)
                //// save metrics
                .toTable(Named.as(METRICS_STORE), initializeMetricStore());

        return streamsBuilder.build();
    }

    private Materialized<MetricKey, MetricValue, KeyValueStore<Bytes, byte[]>> initializeMetricStore() {
        return Materialized.<MetricKey, MetricValue, KeyValueStore<Bytes, byte[]>>as(METRICS_STORE)
                .withKeySerde(ProtobufSerdes.MetricKey())
                .withValueSerde(ProtobufSerdes.MetricValue())
                .withRetention(storeRetention);
    }

    private static List<KeyValue<MetricKey, MetricValue>> makeLatencyMetrics(
            final BeatKey key, final AverageAggregator value) {
        final String metricIdPrefix = key.getType().toString().toLowerCase();
        return List.of(
                KeyValue.pair(
                        buildMetricKey(key, "%s_%s".formatted(metricIdPrefix, "avg")),
                        buildMetricValue(value.getAvg())),
                KeyValue.pair(
                        buildMetricKey(key, "%s_%s".formatted(metricIdPrefix, "max")),
                        buildMetricValue(value.getMax())));
    }

    private static MetricValue buildMetricValue(final double value) {
        return MetricValue.newBuilder().setValue(value).build();
    }

    private static MetricKey buildMetricKey(final BeatKey key, final String id) {
        return MetricKey.newBuilder()
                .setServerVersion(key.getServerVersion())
                .setServerPort(key.getServerPort())
                .setServerHost(key.getServerHost())
                .setId(id)
                .build();
    }

    private Materialized<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>> initializeLatencyStore() {
        return Materialized.<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>>as(LATENCY_AVG_STORE)
                .withKeySerde(ProtobufSerdes.BeatKey())
                .withValueSerde(ProtobufSerdes.AverageAggregator())
                .withRetention(storeRetention);
    }

    private static AverageAggregator initializeAverageAggregator() {
        return AverageAggregator.newBuilder().build();
    }

    private static BeatKey cleanBeatKeyId(final BeatKey key) {
        return BeatKey.newBuilder()
                .setType(key.getType())
                .setServerVersion(key.getServerVersion())
                .setServerHost(key.getServerHost())
                .setServerPort(key.getServerPort())
                .build();
    }

    private static AverageAggregator aggregateAverage(
            final BeatKey key, final BeatValue value, final AverageAggregator aggregate) {
        final long count = aggregate.getCount() + 1L;
        final double sum = aggregate.getSum() + value.getLatency();
        final double avg = sum / count;
        final double max = Math.max(value.getLatency(), aggregate.getMax());

        return AverageAggregator.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAvg(avg)
                .setMax(max)
                .build();
    }

    private static void peekMetrics(final MetricKey key, final MetricValue value) {
        log.debug(
                "server={}:{}, id={}, value={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getId(),
                value.getValue());
    }
}
