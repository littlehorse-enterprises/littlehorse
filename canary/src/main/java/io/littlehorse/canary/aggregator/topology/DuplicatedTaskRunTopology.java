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
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.SessionStore;

@Slf4j
public class DuplicatedTaskRunTopology {

    public static final String DUPLICATED_TASK_RUN_WINDOWS = "duplicated-task-run-windows";
    public static final String DUPLICATED_TASK_RUN = "duplicated-task-run";

    public DuplicatedTaskRunTopology(final KStream<BeatKey, Beat> mainStream) {
        mainStream
                .filter((key, value) -> value.hasTaskRunBeat())
                .groupByKey()
                // open a window sessions
                .windowedBy(SessionWindows.ofInactivityGapAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                // count all the records with the same key
                .aggregate(
                        () -> 0,
                        (key, value, aggregate) -> aggregate + 1,
                        (aggKey, aggOne, aggTwo) -> aggOne + aggTwo,
                        Materialized.<BeatKey, Integer, SessionStore<Bytes, byte[]>>as(DUPLICATED_TASK_RUN_WINDOWS)
                                .withKeySerde(ProtobufSerdes.BeatKey())
                                .withValueSerde(Serdes.Integer()))
                .toStream((key, value) -> key.key())
                // filter by duplicated
                .filterNot((key, value) -> value == null)
                .filter((key, value) -> value > 1)
                // peek aggregate
                .peek((key, value) -> peekAggregate(key, value))
                // re-key by lh cluster
                .groupBy(
                        (key, value) -> toMetricKey(key),
                        Grouped.<MetricKey, Integer>as(DUPLICATED_TASK_RUN)
                                .withKeySerde(ProtobufSerdes.MetricKey())
                                .withValueSerde(Serdes.Integer()))
                // create store
                .reduce(
                        (value1, value2) -> value1 + value2,
                        Materialized.<MetricKey, Integer, KeyValueStore<Bytes, byte[]>>as(DUPLICATED_TASK_RUN)
                                .withKeySerde(ProtobufSerdes.MetricKey())
                                .withValueSerde(Serdes.Integer()));
    }

    private static MetricKey toMetricKey(final BeatKey key) {
        return MetricKey.newBuilder()
                .setId("duplicated_task_run_count")
                .setServerHost(key.getServerHost())
                .setServerPort(key.getServerPort())
                .setServerVersion(key.getServerVersion())
                .build();
    }

    private static void peekAggregate(final BeatKey key, final Integer count) {
        log.debug(
                "server={}:{}, idempotency_key={}, attempt_number={}, count={}",
                key.getServerHost(),
                key.getServerPort(),
                key.getTaskRunBeatKey().getIdempotencyKey(),
                key.getTaskRunBeatKey().getAttemptNumber(),
                count);
    }
}
