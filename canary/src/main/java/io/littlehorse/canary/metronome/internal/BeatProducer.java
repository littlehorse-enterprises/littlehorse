package io.littlehorse.canary.metronome.internal;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.Tag;
import io.littlehorse.canary.util.ShutdownHook;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class BeatProducer {

    private final Producer<Bytes, Bytes> producer;
    private final Map<String, String> extraTags;
    private final String lhServerHost;
    private final int lhServerPort;
    private final String lhServerVersion;
    private final String topicName;

    public BeatProducer(
            final String lhServerHost,
            final int lhServerPort,
            final String lhServerVersion,
            final String topicName,
            final Map<String, Object> producerConfig,
            final Map<String, String> extraTags) {
        this.lhServerHost = lhServerHost;
        this.lhServerPort = lhServerPort;
        this.lhServerVersion = lhServerVersion;
        this.topicName = topicName;
        this.extraTags = extraTags;

        producer = new KafkaProducer<>(producerConfig);
        ShutdownHook.add("Beat Producer", producer);
    }

    public Future<RecordMetadata> sendFuture(final String id, final BeatType type) {
        return sendFuture(id, type, null, null);
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

    public RecordMetadata send(final String id, final BeatType type, final Duration latency) {
        try {
            return sendFuture(id, type, null, latency).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CanaryException(e);
        }
    }

    private BeatValue buildValue(final Duration latency) {
        final BeatValue.Builder builder = BeatValue.newBuilder().setTime(Timestamps.now());

        if (latency != null) {
            builder.setLatency(latency.toMillis());
        }

        return builder.build();
    }

    private BeatKey buildKey(final String id, final BeatType type, final String status) {
        final BeatKey.Builder builder = BeatKey.newBuilder()
                .setServerHost(lhServerHost)
                .setServerPort(lhServerPort)
                .setServerVersion(lhServerVersion)
                .setId(id)
                .setType(type);

        if (status != null) {
            builder.setStatus(status);
        }

        final List<Tag> tags = extraTags.entrySet().stream()
                .map(entry -> Tag.newBuilder()
                        .setKey(entry.getKey())
                        .setValue(entry.getValue())
                        .build())
                .toList();

        builder.addAllTags(tags);

        return builder.build();
    }
}
