package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.AverageAggregator;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.MetricKey;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

@Slf4j
public class LatencyTopology {

    public static final String LATENCY_METRICS_STORE = "latency-metrics";
    public static final String LATENCY_WINDOWS_STORE = "latency-windows";

    public LatencyTopology(final KStream<BeatKey, Beat> mainStream) {
        mainStream
                .filter((key, value) -> value.hasLatencyBeat())
                .groupByKey()
                // reset aggregator every minute
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                .aggregate(
                        () -> AverageAggregator.newBuilder().build(),
                        (key, value, aggregate) -> aggregate(value, aggregate),
                        Materialized.<BeatKey, AverageAggregator, WindowStore<Bytes, byte[]>>as(LATENCY_WINDOWS_STORE)
                                .withKeySerde(ProtobufSerdes.BeatKey())
                                .withValueSerde(ProtobufSerdes.AverageAggregator()))
                .toStream((key, value) -> key.key())
                // peek aggregate
                .peek((key, value) -> peekAggregate(key, value))
                // extract metrics
                .flatMap((key, value) -> makeMetrics(key, value))
                // create store
                .toTable(
                        Named.as(LATENCY_METRICS_STORE),
                        Materialized.<MetricKey, Double, KeyValueStore<Bytes, byte[]>>as(LATENCY_METRICS_STORE)
                                .withKeySerde(ProtobufSerdes.MetricKey())
                                .withValueSerde(Serdes.Double()));
    }

    private static List<KeyValue<MetricKey, Double>> makeMetrics(final BeatKey key, final AverageAggregator value) {
        return List.of(
                KeyValue.pair(buildMetricKey(key, "avg"), value.getAvg()),
                KeyValue.pair(buildMetricKey(key, "max"), value.getMax()));
    }

    private static MetricKey buildMetricKey(final BeatKey key, final String suffix) {
        return MetricKey.newBuilder()
                .setServerVersion(key.getServerVersion())
                .setServerPort(key.getServerPort())
                .setServerHost(key.getServerHost())
                .setId("%s_%s".formatted(key.getLatencyBeatKey().getName(), suffix))
                .build();
    }

    private static void peekAggregate(final BeatKey key, final AverageAggregator value) {
        log.debug(
                "server={}:{}, latency={}, count={}, sum={}, avg={}, max={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getLatencyBeatKey().getName(),
                value.getCount(),
                value.getSum(),
                value.getAvg(),
                value.getMax());
    }

    private static AverageAggregator aggregate(final Beat value, final AverageAggregator aggregate) {
        final int count = aggregate.getCount() + 1;
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
