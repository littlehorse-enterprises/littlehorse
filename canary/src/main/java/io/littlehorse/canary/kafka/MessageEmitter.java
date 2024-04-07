package io.littlehorse.canary.kafka;

import com.google.protobuf.Message;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class MessageEmitter implements MeterBinder {

    private final Producer<Bytes, Bytes> producer;
    private final String topicName;

    public MessageEmitter(final String topicName, final Map<String, Object> kafkaProducerConfigMap) {
        this.topicName = topicName;
        producer = new KafkaProducer<>(kafkaProducerConfigMap);
        Shutdown.addShutdownHook("Message Emitter", producer);
    }

    /**
     * Asynchronous method that produces a value to kafka but does not block the current thread
     *
     * @param key
     * @param value
     * @return Future<RecordMetadata>
     */
    public Future<RecordMetadata> future(final Message key, final Message value) {
        final ProducerRecord<Bytes, Bytes> record =
                new ProducerRecord<>(topicName, Bytes.wrap(key.toByteArray()), Bytes.wrap(value.toByteArray()));

        return producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.trace("Emitting message {}", key.getClass());
            } else {
                log.error("Emitting message {}", key.getClass(), exception);
            }
        });
    }

    /**
     * Blocking method that produces a value and waits until kafka acknowledges
     *
     * @param key
     * @param value
     * @return RecordMetadata
     */
    public RecordMetadata emit(final Message key, final Message value) {
        try {
            return future(key, value).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CanaryException(e);
        }
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final KafkaClientMetrics kafkaClientMetrics = new KafkaClientMetrics(producer);
        Shutdown.addShutdownHook("Metrics Emitter: Prometheus Exporter", kafkaClientMetrics);
        kafkaClientMetrics.bindTo(registry);
    }
}
