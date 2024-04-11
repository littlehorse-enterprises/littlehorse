package io.littlehorse.canary.kafka;

import java.util.Map;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.Bytes;

public class BeatConsumer {

    public BeatConsumer(final Map<String, Object> config) {
        final KafkaConsumer<Bytes, Bytes> consumer = new KafkaConsumer<>(config);
    }
}
