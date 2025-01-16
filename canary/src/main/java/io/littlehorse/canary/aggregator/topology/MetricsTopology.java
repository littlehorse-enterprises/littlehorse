package io.littlehorse.canary.aggregator.topology;

import com.google.common.base.Strings;
import io.littlehorse.canary.aggregator.internal.BeatTimeExtractor;
import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.time.Duration;
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
    public static final String DUPLICATED_TASK_RUN_STORE = "duplicated-task-run";
    public static final String DUPLICATED_TASK_RUN_BY_SERVER_STORE = "duplicated-task-run-by-server";

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

        // build latency metric stream
        final KStream<MetricKey, MetricValue> latencyMetricsStream = beatsStream
                // remove GET_WF_RUN_EXHAUSTED_RETRIES
                .filterNot(MetricsTopology::isExhaustedRetries)
                // remove the id
                .groupBy(
                        MetricsTopology::removeWfId, Grouped.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue()))
                // reset aggregator every minute
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                // calculate average
                .aggregate(
                        MetricsTopology::initializeAverageAggregator,
                        MetricsTopology::aggregateAverage,
                        initializeLatencyStore(LATENCY_STORE))
                // build metric
                .toStream(MetricsTopology::extractKeyFromWindow)
                .map(MetricsTopology::mapBeatToMetricLatency);

        // build count metric stream
        final KStream<MetricKey, MetricValue> countMetricStream = beatsStream
                // remove the id
                .groupBy(
                        MetricsTopology::removeWfId, Grouped.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue()))
                // count all
                .count(initializeBeatCountStore(COUNT_STORE))
                .toStream()
                .map(MetricsTopology::mapBeatToMetricCount);

        // build duplicated task run stream
        final KStream<MetricKey, MetricValue> duplicatedTaskRunStream = beatsStream
                // filter by TASK_RUN_EXECUTION
                .filter(MetricsTopology::filterTaskRunBeats)
                .groupByKey()
                // count all the records with the same idempotency key and attempt number
                .count(initializeBeatCountStore(DUPLICATED_TASK_RUN_STORE))
                // filter by duplicated
                .filter(MetricsTopology::selectDuplicatedTaskRun)
                // group by server
                .groupBy(
                        MetricsTopology::mapBeatToDuplicatedTaskRunMetric,
                        Grouped.with(ProtobufSerdes.MetricKey(), Serdes.Long()))
                // count by server
                .count(initializeMetricCountStore(DUPLICATED_TASK_RUN_BY_SERVER_STORE))
                .toStream()
                .mapValues(MetricsTopology::mapLongToMetricValue);

        // merge streams
        latencyMetricsStream
                .merge(countMetricStream)
                .merge(duplicatedTaskRunStream)
                // group by metric
                .groupByKey(Grouped.with(ProtobufSerdes.MetricKey(), ProtobufSerdes.MetricValue()))
                // aggregate metric values
                .aggregate(
                        MetricsTopology::initializeMetricAggregator,
                        MetricsTopology::aggregateMetrics,
                        initializeMetricStore(METRICS_STORE));

        return streamsBuilder.build();
    }

    private static MetricValue aggregateMetrics(
            final MetricKey metricKey, final MetricValue metricValue, final MetricValue aggregator) {
        return MetricValue.newBuilder()
                // previous values
                .putAllValues(aggregator.getValuesMap())
                // new values
                .putAllValues(metricValue.getValuesMap())
                .build();
    }

    private static MetricValue initializeMetricAggregator() {
        return MetricValue.newBuilder().build();
    }

    private static boolean isExhaustedRetries(final BeatKey key, final BeatValue value) {
        return key.getType().equals(BeatType.GET_WF_RUN_EXHAUSTED_RETRIES);
    }

    private static boolean selectDuplicatedTaskRun(final BeatKey key, final Long value) {
        return value > 1L;
    }

    private static MetricValue mapLongToMetricValue(final Long value) {
        return MetricValue.newBuilder().putValues("count", value).build();
    }

    private static MetricValue mapAvgToMetricValue(final Double avg, final Double max) {
        return MetricValue.newBuilder()
                .putValues("avg", avg)
                .putValues("max", max)
                .build();
    }

    private Materialized<MetricKey, Long, KeyValueStore<Bytes, byte[]>> initializeMetricCountStore(
            final String storeName) {
        return Materialized.<MetricKey, Long, KeyValueStore<Bytes, byte[]>>as(storeName)
                .withKeySerde(ProtobufSerdes.MetricKey())
                .withValueSerde(Serdes.Long())
                .withRetention(storeRetention);
    }

    private static KeyValue<MetricKey, Long> mapBeatToDuplicatedTaskRunMetric(final BeatKey key, final Long count) {
        return KeyValue.pair(buildMetricKey(key, "duplicated_task_run"), count);
    }

    private static boolean filterTaskRunBeats(final BeatKey key, final BeatValue value) {
        return key.getType().equals(BeatType.TASK_RUN_EXECUTION);
    }

    private static BeatKey extractKeyFromWindow(final Windowed<BeatKey> keyWindowed, final AverageAggregator value) {
        return keyWindowed.key();
    }

    private Materialized<BeatKey, Long, KeyValueStore<Bytes, byte[]>> initializeBeatCountStore(final String storeName) {
        return Materialized.<BeatKey, Long, KeyValueStore<Bytes, byte[]>>as(storeName)
                .withKeySerde(ProtobufSerdes.BeatKey())
                .withValueSerde(Serdes.Long())
                .withRetention(storeRetention);
    }

    private static KeyValue<MetricKey, MetricValue> mapBeatToMetricCount(final BeatKey key, final Long count) {
        final String metricIdPrefix = key.getType().toString().toLowerCase();
        return KeyValue.pair(buildMetricKey(key, metricIdPrefix), mapLongToMetricValue(count));
    }

    private static Consumed<BeatKey, BeatValue> initializeSerdes() {
        return Consumed.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue())
                .withTimestampExtractor(new BeatTimeExtractor());
    }

    private Materialized<MetricKey, MetricValue, KeyValueStore<Bytes, byte[]>> initializeMetricStore(
            final String storeName) {
        return Materialized.<MetricKey, MetricValue, KeyValueStore<Bytes, byte[]>>as(storeName)
                .withKeySerde(ProtobufSerdes.MetricKey())
                .withValueSerde(ProtobufSerdes.MetricValue())
                .withRetention(storeRetention);
    }

    private static KeyValue<MetricKey, MetricValue> mapBeatToMetricLatency(
            final BeatKey key, final AverageAggregator value) {
        final String metricIdPrefix = key.getType().toString().toLowerCase();
        return KeyValue.pair(buildMetricKey(key, metricIdPrefix), mapAvgToMetricValue(value.getAvg(), value.getMax()));
    }

    private static MetricKey buildMetricKey(final BeatKey key, final String id) {
        final MetricKey.Builder builder = MetricKey.newBuilder()
                .setServerVersion(key.getServerVersion())
                .setServerPort(key.getServerPort())
                .setServerHost(key.getServerHost())
                .setId("canary_%s".formatted(id));

        if (key.hasStatus() && !Strings.isNullOrEmpty(key.getStatus())) {
            builder.addTags(
                    Tag.newBuilder().setKey("status").setValue(key.getStatus().toLowerCase()));
        }

        if (key.getTagsCount() > 0) {
            builder.addAllTags(key.getTagsList());
        }

        return builder.build();
    }

    private Materialized<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>> initializeLatencyStore(
            final String storeName) {
        return Materialized.<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>>as(storeName)
                .withKeySerde(ProtobufSerdes.BeatKey())
                .withValueSerde(ProtobufSerdes.AverageAggregator())
                .withRetention(storeRetention);
    }

    private static AverageAggregator initializeAverageAggregator() {
        return AverageAggregator.newBuilder().build();
    }

    private static BeatKey removeWfId(final BeatKey key, final BeatValue value) {
        return BeatKey.newBuilder()
                .setType(key.getType())
                .setServerVersion(key.getServerVersion())
                .setServerHost(key.getServerHost())
                .setServerPort(key.getServerPort())
                .setStatus(key.getStatus())
                .addAllTags(key.getTagsList())
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
}
