package io.littlehorse.canary.metronome.internal;

import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.metronome.model.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.Tag;
import java.util.List;
import java.util.Map;
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

    public Future<RecordMetadata> send(final Beat beat) {
        final BeatKey beatKey = beat.toBeatKey().toBuilder()
                .setServerHost(lhServerHost)
                .setServerPort(lhServerPort)
                .setServerVersion(lhServerVersion)
                .addAllTags(getExtraTags())
                .build();
        final BeatValue beatValue = beat.toBeatValue();
        final ProducerRecord<Bytes, Bytes> record =
                new ProducerRecord<>(topicName, Bytes.wrap(beatKey.toByteArray()), Bytes.wrap(beatValue.toByteArray()));

        return producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.debug("Producing message {}", beatKey.getType());
            } else {
                log.error("Producing message {}", beatKey.getType(), exception);
            }
        });
    }

    private List<Tag> getExtraTags() {
        return extraTags.entrySet().stream()
                .map(entry -> Tag.newBuilder()
                        .setKey(entry.getKey())
                        .setValue(entry.getValue())
                        .build())
                .toList();
    }
}
