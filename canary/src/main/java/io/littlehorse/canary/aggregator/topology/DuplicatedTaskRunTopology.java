package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.aggregator.serdes.ProtobufSerdes;
import io.littlehorse.canary.proto.*;
import java.time.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.jetbrains.annotations.NotNull;

@Getter
@Slf4j
public class DuplicatedTaskRunTopology {

    public static final String DUPLICATED_TASK_COUNT_STORE = "duplicated-task-count";
    public static final String DUPLICATED_TASK_BY_SERVER_COUNT_STORE = "duplicated-task-by-server-count";

    public static final String DUPLICATED_TASK_METRIC_NAME = "duplicated_task_run_max_count";
    public static final String TASK_RUN_LATENCY_METRIC_NAME = "task_run_latency";

    private final KStream<MetricKey, MetricValue> stream;

    public DuplicatedTaskRunTopology(
            final KStream<BeatKey, BeatValue> mainStream, final Duration storeRetention, final String inputTopic) {
        // filter all task run beats
        final KStream<BeatKey, BeatValue> taskRunBeats = mainStream.filter((key, value) -> key.hasTaskRunBeatKey());

        // send latency to another topology
        taskRunBeats
                .map(DuplicatedTaskRunTopology::toLatencyMetricBeat)
                .to(inputTopic, Produced.with(ProtobufSerdes.BeatKey(), ProtobufSerdes.BeatValue()));

        // define this topology
        stream = taskRunBeats
                .groupByKey()
                // count all the records with the same idempotency key and attempt number
                .count(Materialized.<BeatKey, Long, KeyValueStore<Bytes, byte[]>>as(DUPLICATED_TASK_COUNT_STORE)
                        .withKeySerde(ProtobufSerdes.BeatKey())
                        .withValueSerde(Serdes.Long())
                        .withRetention(storeRetention))
                // filter by duplicated
                .filter((key, value) -> value > 1L)
                .toStream()
                // debug peek aggregate
                .peek(DuplicatedTaskRunTopology::peekAggregate)
                // re-key from task run to lh cluster
                .groupBy((key, value) -> toMetricKey(key), Grouped.with(ProtobufSerdes.MetricKey(), Serdes.Long()))
                // count how many task were duplicated
                .count(Materialized.<MetricKey, Long, KeyValueStore<Bytes, byte[]>>as(
                                DUPLICATED_TASK_BY_SERVER_COUNT_STORE)
                        .withKeySerde(ProtobufSerdes.MetricKey())
                        .withValueSerde(Serdes.Long())
                        .withRetention(storeRetention))
                .mapValues((readOnlyKey, value) -> MetricFactory.buildValue(value))
                .toStream();
    }

    @NotNull
    private static KeyValue<BeatKey, BeatValue> toLatencyMetricBeat(final BeatKey oldKey, final BeatValue oldValue) {
        final BeatKey newKey = BeatKey.newBuilder()
                .setServerHost(oldKey.getServerHost())
                .setServerVersion(oldKey.getServerVersion())
                .setServerPort(oldKey.getServerPort())
                .setLatencyBeatKey(LatencyBeatKey.newBuilder().setId(TASK_RUN_LATENCY_METRIC_NAME))
                .build();

        final BeatValue newValue = BeatValue.newBuilder()
                .setTime(oldValue.getTime())
                .setLatencyBeat(LatencyBeat.newBuilder()
                        .setLatency(oldValue.getTaskRunBeat().getLatency()))
                .build();

        return new KeyValue<>(newKey, newValue);
    }

    private static MetricKey toMetricKey(final BeatKey key) {
        return MetricFactory.buildKey(
                DUPLICATED_TASK_METRIC_NAME, key.getServerHost(), key.getServerPort(), key.getServerVersion());
    }

    private static void peekAggregate(final BeatKey key, final Long count) {
        log.debug(
                "server={}:{}, idempotency_key={}, attempt_number={}, count={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getTaskRunBeatKey().getIdempotencyKey(),
                key.getTaskRunBeatKey().getAttemptNumber(),
                count);
    }
}
