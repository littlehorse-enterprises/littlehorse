package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.MetricKey;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class DuplicatedTaskRunTopology {

    private final KStream<MetricKey, Double> stream;

    public DuplicatedTaskRunTopology(final KStream<BeatKey, Beat> mainStream, final Duration storeRetetion) {
        stream = mainStream
                .filter((key, value) -> value.hasTaskRunBeat())
                .groupByKey()
                // count all the records with the same idempotency key and attempt number
                .count(Materialized.<BeatKey, Long, KeyValueStore<Bytes, byte[]>>with(
                                ProtobufSerdes.BeatKey(), Serdes.Long())
                        .withRetention(storeRetetion))
                // filter by duplicated
                .filter((key, value) -> value > 1L)
                .toStream()
                .mapValues((readOnlyKey, value) -> Double.valueOf(value))
                // debug peek aggregate
                .peek((key, value) -> peekAggregate(key, value))
                // re-key from task run to lh cluster
                .groupBy((key, value) -> toMetricKey(key), Grouped.with(ProtobufSerdes.MetricKey(), Serdes.Double()))
                // count how many task were duplicated
                .count(Materialized.<MetricKey, Long, KeyValueStore<Bytes, byte[]>>with(
                                ProtobufSerdes.MetricKey(), Serdes.Long())
                        .withRetention(storeRetetion))
                .mapValues((readOnlyKey, value) -> Double.valueOf(value))
                .toStream();
    }

    private static MetricKey toMetricKey(final BeatKey key) {
        return MetricKey.newBuilder()
                .setId("duplicated_task_run_max_count")
                .setServerHost(key.getServerHost())
                .setServerPort(key.getServerPort())
                .setServerVersion(key.getServerVersion())
                .build();
    }

    private static void peekAggregate(final BeatKey key, final Double count) {
        log.debug(
                "server={}:{}, idempotency_key={}, attempt_number={}, count={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getTaskRunBeatKey().getIdempotencyKey(),
                key.getTaskRunBeatKey().getAttemptNumber(),
                count);
    }

    public KStream<MetricKey, Double> getStream() {
        return stream;
    }
}
