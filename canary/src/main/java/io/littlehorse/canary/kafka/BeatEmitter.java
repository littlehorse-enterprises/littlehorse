package io.littlehorse.canary.kafka;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatStatus;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.util.ShutdownHook;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class BeatEmitter {

    private final Producer<Bytes, Bytes> producer;
    private final String lhServerHost;
    private final int lhServerPort;
    private final String lhServerVersion;
    private final String topicName;

    public BeatEmitter(
            final String lhServerHost,
            final int lhServerPort,
            final String lhServerVersion,
            final String topicName,
            final Map<String, Object> kafkaProducerConfigMap) {
        this.lhServerHost = lhServerHost;
        this.lhServerPort = lhServerPort;
        this.lhServerVersion = lhServerVersion;
        this.topicName = topicName;

        producer = new KafkaProducer<>(kafkaProducerConfigMap);
        ShutdownHook.add("Metrics Emitter", producer);
    }

    public Future<RecordMetadata> future(
            final String id, final BeatType type, final BeatStatus status, final Duration latency) {
        final BeatKey beatKey = buildKey(id, type);
        final BeatValue beatValue = buildValue(latency, status);

        final ProducerRecord<Bytes, Bytes> record =
                new ProducerRecord<>(topicName, Bytes.wrap(beatKey.toByteArray()), Bytes.wrap(beatValue.toByteArray()));

        return producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.trace("Emitting message {}", beatKey.getType());
            } else {
                log.error("Emitting message {}", beatKey.getType(), exception);
            }
        });
    }

    public RecordMetadata emit(final String id, final BeatType type, final BeatStatus status, final Duration latency) {
        try {
            return future(id, type, status, latency).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CanaryException(e);
        }
    }

    private BeatValue buildValue(final Duration latency, final BeatStatus status) {
        return BeatValue.newBuilder()
                .setTime(Timestamps.now())
                .setLatency(latency.toMillis())
                .setStatus(status)
                .build();
    }

    private BeatKey buildKey(final String id, final BeatType type) {
        return BeatKey.newBuilder()
                .setServerHost(lhServerHost)
                .setServerPort(lhServerPort)
                .setServerVersion(lhServerVersion)
                .setId(id)
                .setType(type)
                .build();
    }
}
