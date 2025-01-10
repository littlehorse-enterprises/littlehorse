package io.littlehorse.canary.aggregator.topology;

import com.google.common.base.Strings;
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
                        initializeLatencyStore())
                // build metric
                .toStream(MetricsTopology::extractKeyFromWindow)
                .flatMap(MetricsTopology::makeLatencyMetrics);

        // build count metric stream
        final KStream<MetricKey, MetricValue> countMetricStream = beatsStream
                // remove the id
                .groupBy(
                        MetricsTopology::removeWfId, Grouped.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue()))
                // count all
                .count(initializeCountStore(COUNT_STORE))
                .toStream()
                .map(MetricsTopology::makeCountMetric);

        // build duplicated task run stream
        final KStream<MetricKey, MetricValue> duplicatedTaskRunStream = beatsStream
                // filter by TASK_RUN_EXECUTION
                .filter(MetricsTopology::filterTaskRunBeats)
                .groupByKey()
                // count all the records with the same idempotency key and attempt number
                .count(initializeCountStore(DUPLICATED_TASK_RUN_STORE))
                // filter by duplicated
                .filter(MetricsTopology::selectDuplicatedTaskRun)
                // group by server
                .groupBy(
                        MetricsTopology::buildDuplicatedMetric, Grouped.with(ProtobufSerdes.MetricKey(), Serdes.Long()))
                // count by server
                .count(initializeDuplicatedTaskRunStore())
                .toStream()
                .mapValues(MetricsTopology::makeDuplicatedTaskRunCountMetric);

        // merge streams
        latencyMetricsStream
                .merge(countMetricStream)
                .merge(duplicatedTaskRunStream)
                // peek
                .peek(MetricsTopology::peekMetrics)
                // save metrics
                .toTable(Named.as(METRICS_STORE), initializeMetricStore());

        return streamsBuilder.build();
    }

    private static boolean isExhaustedRetries(final BeatKey key, final BeatValue value) {
        return key.getType().equals(BeatType.GET_WF_RUN_EXHAUSTED_RETRIES);
    }

    private static boolean selectDuplicatedTaskRun(final BeatKey key, final Long value) {
        return value > 1L;
    }

    private static MetricValue makeDuplicatedTaskRunCountMetric(final MetricKey readOnlyKey, final Long value) {
        return buildMetricValue(value);
    }

    private Materialized<MetricKey, Long, KeyValueStore<Bytes, byte[]>> initializeDuplicatedTaskRunStore() {
        return Materialized.<MetricKey, Long, KeyValueStore<Bytes, byte[]>>as(DUPLICATED_TASK_RUN_BY_SERVER_STORE)
                .withKeySerde(ProtobufSerdes.MetricKey())
                .withValueSerde(Serdes.Long())
                .withRetention(storeRetention);
    }

    private static KeyValue<MetricKey, Long> buildDuplicatedMetric(final BeatKey key, final Long count) {
        return KeyValue.pair(buildMetricKey(key, "duplicated_task_run_count"), count);
    }

    private static boolean filterTaskRunBeats(final BeatKey key, final BeatValue value) {
        return key.getType().equals(BeatType.TASK_RUN_EXECUTION);
    }

    private static BeatKey extractKeyFromWindow(final Windowed<BeatKey> keyWindowed, final AverageAggregator value) {
        return keyWindowed.key();
    }

    private Materialized<BeatKey, Long, KeyValueStore<Bytes, byte[]>> initializeCountStore(final String storeName) {
        return Materialized.<BeatKey, Long, KeyValueStore<Bytes, byte[]>>as(storeName)
                .withKeySerde(ProtobufSerdes.BeatKey())
                .withValueSerde(Serdes.Long())
                .withRetention(storeRetention);
    }

    private static KeyValue<MetricKey, MetricValue> makeCountMetric(final BeatKey key, final Long count) {
        final String metricIdPrefix = key.getType().toString().toLowerCase();
        return KeyValue.pair(buildMetricKey(key, "%s_%s".formatted(metricIdPrefix, "count")), buildMetricValue(count));
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

    private Materialized<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>> initializeLatencyStore() {
        return Materialized.<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>>as(LATENCY_STORE)
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
