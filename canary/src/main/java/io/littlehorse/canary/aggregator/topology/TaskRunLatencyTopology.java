package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.MetricAverage;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class TaskRunLatencyTopology {

    public TaskRunLatencyTopology(final KStream<String, Metric> metricStream) {
        metricStream
                .filter((key, value) -> value.hasTaskRunLatency())
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                .aggregate(
                        () -> MetricAverage.newBuilder().build(),
                        (key, value, aggregate) -> aggregate(value, aggregate),
                        Named.as("test"),
                        Materialized.with(Serdes.String(), ProtobufSerdes.MetricAverage()))
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value))
                .peek((key, value) -> log.debug(
                        "server={}, count={}, sum={}, avg={}", key, value.getCount(), value.getSum(), value.getAvg()))
                .toTable(Materialized.<String, MetricAverage, KeyValueStore<Bytes, byte[]>>as("latency-metrics")
                        .with(Serdes.String(), ProtobufSerdes.MetricAverage()));
    }

    private static MetricAverage aggregate(final Metric value, final MetricAverage aggregate) {
        final long count = aggregate.getCount() + 1;
        final double sum = aggregate.getSum() + value.getTaskRunLatency().getLatency();
        final double avg = sum / count;
        return MetricAverage.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAvg(avg)
                .build();
    }
}
