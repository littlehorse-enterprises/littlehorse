package io.littlehorse.canary.metronome.internal;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.proto.BeatKey;
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
public class BeatProducer {

    private final Producer<Bytes, Bytes> producer;
    private final String lhServerHost;
    private final int lhServerPort;
    private final String lhServerVersion;
    private final String topicName;

    public BeatProducer(
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
        ShutdownHook.add("Beat Producer", producer);
    }

    public Future<RecordMetadata> sendFuture(
            final String id, final BeatType type, final String status, final Duration latency) {

        final BeatKey beatKey = buildKey(id, type, status);
        final BeatValue beatValue = buildValue(latency);

        return producer.send(buildRecord(beatKey, beatValue), (metadata, exception) -> {
            if (exception == null) {
                log.trace("Producing message {}", beatKey.getType());
            } else {
                log.error("Producing message {}", beatKey.getType(), exception);
            }
        });
    }

    private ProducerRecord<Bytes, Bytes> buildRecord(final BeatKey beatKey, final BeatValue beatValue) {
        return new ProducerRecord<>(topicName, Bytes.wrap(beatKey.toByteArray()), Bytes.wrap(beatValue.toByteArray()));
    }

    public RecordMetadata send(final String id, final BeatType type, final String status, final Duration latency) {
        try {
            return sendFuture(id, type, status, latency).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CanaryException(e);
        }
    }

    private BeatValue buildValue(final Duration latency) {
        return BeatValue.newBuilder()
                .setTime(Timestamps.now())
                .setLatency(latency.toMillis())
                .build();
    }

    private BeatKey buildKey(final String id, final BeatType type, final String status) {
        return BeatKey.newBuilder()
                .setServerHost(lhServerHost)
                .setServerPort(lhServerPort)
                .setServerVersion(lhServerVersion)
                .setId(id)
                .setType(type)
                .setStatus(status)
                .build();
    }
}
