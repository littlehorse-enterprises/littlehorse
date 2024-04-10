package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.MetricKey;
import java.time.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Getter
@Slf4j
public class DuplicatedTaskRunTopology {

    public static final String DUPLICATED_TASK_COUNT_STORE = "duplicated-task-count";
    public static final String DUPLICATED_TASK_BY_SERVER_COUNT_STORE = "duplicated-task-by-server-count";
    public static final String DUPLICATED_TASK_METRIC_NAME = "duplicated_task_run_max_count";
    private final KStream<MetricKey, Double> stream;

    public DuplicatedTaskRunTopology(final KStream<BeatKey, BeatValue> mainStream, final Duration storeRetention) {
        stream = mainStream
                //                .filter((key, value) -> value.hasTaskRunBeat())
                .groupByKey()
                // count all the records with the same idempotency key and attempt number
                .count(Materialized.<BeatKey, Long, KeyValueStore<Bytes, byte[]>>as(DUPLICATED_TASK_COUNT_STORE)
                        .withKeySerde(ProtobufSerdes.BeatKey())
                        .withValueSerde(Serdes.Long())
                        .withRetention(storeRetention))
                // filter by duplicated
                .filter((key, value) -> value > 1L)
                .toStream()
                .mapValues((readOnlyKey, value) -> Double.valueOf(value))
                // debug peek aggregate
                .peek(DuplicatedTaskRunTopology::peekAggregate)
                // re-key from task run to lh cluster
                .groupBy((key, value) -> toMetricKey(key), Grouped.with(ProtobufSerdes.MetricKey(), Serdes.Double()))
                // count how many task were duplicated
                .count(Materialized.<MetricKey, Long, KeyValueStore<Bytes, byte[]>>as(
                                DUPLICATED_TASK_BY_SERVER_COUNT_STORE)
                        .withKeySerde(ProtobufSerdes.MetricKey())
                        .withValueSerde(Serdes.Long())
                        .withRetention(storeRetention))
                .mapValues((readOnlyKey, value) -> Double.valueOf(value))
                .toStream();
    }

    private static MetricKey toMetricKey(final BeatKey key) {
        return MetricKey.newBuilder()
                .setId(DUPLICATED_TASK_METRIC_NAME)
                .setServerHost(key.getServerHost())
                .setServerPort(key.getServerPort())
                .setServerVersion(key.getServerVersion())
                .build();
    }

    private static void peekAggregate(final BeatKey key, final Double count) {
        log.debug("server={}:{}, id={}, count={}", key.getServerHost(), key.getServerPort(), key.getId(), count);
    }
}
