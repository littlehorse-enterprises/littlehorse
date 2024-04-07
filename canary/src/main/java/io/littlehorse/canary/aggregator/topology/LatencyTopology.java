package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;

@Getter
@Slf4j
public class LatencyTopology {

    public static final String LATENCY_AVG_STORE = "latency-avg";
    private final KStream<MetricKey, Metric> stream;

    public LatencyTopology(
            final KStream<BeatKey, Beat> mainStream, final TimeWindows windows, final Duration storeRetention) {
        stream = mainStream
                .filter((key, value) -> value.hasLatencyBeat())
                .groupByKey()
                // reset aggregator every minute
                .windowedBy(windows)
                // calculate average
                .aggregate(
                        () -> AverageAggregator.newBuilder().build(),
                        (key, value, aggregate) -> aggregate(value, aggregate),
                        Materialized.<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>>as(LATENCY_AVG_STORE)
                                .withKeySerde(ProtobufSerdes.BeatKey())
                                .withValueSerde(ProtobufSerdes.AverageAggregator())
                                .withRetention(storeRetention))
                .toStream((key, value) -> key.key())
                // debug peek aggregate
                .peek(LatencyTopology::peekAggregate)
                // extract metrics
                .flatMap(LatencyTopology::makeMetrics);
    }

    private static List<KeyValue<MetricKey, Metric>> makeMetrics(final BeatKey key, final AverageAggregator value) {
        return List.of(
                KeyValue.pair(buildMetricKey(key, "avg"), buildMetric(value.getAvg())),
                KeyValue.pair(buildMetricKey(key, "max"), buildMetric(value.getMax())));
    }

    private static Metric buildMetric(final double value) {
        return Metric.newBuilder().setValue(value).build();
    }

    private static MetricKey buildMetricKey(final BeatKey key, final String suffix) {
        return MetricFactory.buildKey(
                "%s_%s".formatted(key.getLatencyBeatKey().getId(), suffix),
                key.getServerHost(),
                key.getServerPort(),
                key.getServerVersion());
    }

    private static void peekAggregate(final BeatKey key, final AverageAggregator value) {
        log.debug(
                "server={}:{}, latency={}, count={}, sum={}, avg={}, max={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getLatencyBeatKey().getId(),
                value.getCount(),
                value.getSum(),
                value.getAvg(),
                value.getMax());
    }

    private static AverageAggregator aggregate(final Beat value, final AverageAggregator aggregate) {
        final long count = aggregate.getCount() + 1L;
        final double sum = aggregate.getSum() + value.getLatencyBeat().getLatency();
        final double avg = sum / count;
        final double max = Math.max(value.getLatencyBeat().getLatency(), aggregate.getMax());

        return AverageAggregator.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAvg(avg)
                .setMax(max)
                .build();
    }
}
