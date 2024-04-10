package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
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
    public static final String LATENCY_STORE = "latency";
    public static final String COUNT_STORE = "count";

    private final String inputTopic;
    private final Duration storeRetention;

    public MetricsTopology(final String inputTopic, final Duration storeRetention) {
        this.inputTopic = inputTopic;
        this.storeRetention = storeRetention;
    }

    public Topology toTopology() {
        // initialize stream
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<BeatKey, BeatValue> beatsStream = streamsBuilder.stream(inputTopic, initializeSerdes());

        // group cleaned beat
        final KGroupedStream<BeatKey, BeatValue> beatsWithoutIdGroup = beatsStream
                //// clean beata to calculate latency
                .groupBy(MetricsTopology::cleanBeatKey, initializeBeatGroup());

        // build latency metric stream
        final KStream<MetricKey, MetricValue> latencyMetricsStream = beatsWithoutIdGroup
                //// reset aggregator every minute
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                //// calculate average
                .aggregate(
                        MetricsTopology::initializeAverageAggregator,
                        MetricsTopology::aggregateAverage,
                        initializeLatencyStore())
                //// build metric
                .toStream((keyWindowed, value) -> keyWindowed.key())
                .flatMap(MetricsTopology::makeLatencyMetrics);

        // build count metric stream
        final KStream<MetricKey, MetricValue> countMetricStream =
                beatsWithoutIdGroup.count(initializeCountStore()).toStream().map(MetricsTopology::makeCountMetric);

        // merge streams
        latencyMetricsStream
                .merge(countMetricStream)
                //// peek
                .peek(MetricsTopology::peekMetrics)
                //// save metrics
                .toTable(Named.as(METRICS_STORE), initializeMetricStore());

        return streamsBuilder.build();
    }

    private Materialized<BeatKey, Long, KeyValueStore<Bytes, byte[]>> initializeCountStore() {
        return Materialized.<BeatKey, Long, KeyValueStore<Bytes, byte[]>>as(COUNT_STORE)
                .withKeySerde(ProtobufSerdes.BeatKey())
                .withValueSerde(Serdes.Long())
                .withRetention(storeRetention);
    }

    private static KeyValue<MetricKey, MetricValue> makeCountMetric(final BeatKey key, final Long count) {
        final String metricIdPrefix = key.getType().toString().toLowerCase();
        return KeyValue.pair(buildMetricKey(key, "%s_%s".formatted(metricIdPrefix, "count")), buildMetricValue(count));
    }

    private static Grouped<BeatKey, BeatValue> initializeBeatGroup() {
        return Grouped.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue());
    }

    private static Consumed<BeatKey, BeatValue> initializeSerdes() {
        return Consumed.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue())
                .withTimestampExtractor(new BeatTimeExtractor());
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
                .setId("canary_%s".formatted(id))
                .addTags(Tag.newBuilder()
                        .setKey("status")
                        .setValue(key.getStatus().toString().toLowerCase()))
                .build();
    }

    private Materialized<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>> initializeLatencyStore() {
        return Materialized.<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>>as(LATENCY_STORE)
                .withKeySerde(ProtobufSerdes.BeatKey())
                .withValueSerde(ProtobufSerdes.AverageAggregator())
                .withRetention(storeRetention);
    }

    private static AverageAggregator initializeAverageAggregator() {
        return AverageAggregator.newBuilder().build();
    }

    private static BeatKey cleanBeatKey(final BeatKey key, final BeatValue value) {
        return BeatKey.newBuilder()
                .setType(key.getType())
                .setServerVersion(key.getServerVersion())
                .setServerHost(key.getServerHost())
                .setServerPort(key.getServerPort())
                .setStatus(key.getStatus())
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
                "server={}:{}, id={}, tags={}, value={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getId(),
                key.getTagsList().stream()
                        .map(tag -> "%s:%s".formatted(tag.getKey(), tag.getValue()))
                        .collect(Collectors.joining(" ")),
                value.getValue());
    }
}
