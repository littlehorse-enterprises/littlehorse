package io.littlehorse.canary.kafka;

import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.proto.Metric;
import java.io.Closeable;
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
public class MetricsEmitter implements Closeable {

    private final Producer<String, Bytes> producer;
    private final String topicName;

    public MetricsEmitter(final String topicName, final Map<String, Object> kafkaProducerConfigMap) {
        this.producer = new KafkaProducer<>(kafkaProducerConfigMap);
        this.topicName = topicName;
    }

    public Future<RecordMetadata> future(final String key, final Metric metric) {
        final ProducerRecord<String, Bytes> record =
                new ProducerRecord<>(topicName, key, Bytes.wrap(metric.toByteArray()));

        return producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.debug("Emitting message {} {}", metric.getMetricCase(), key);
            } else {
                log.error("Emitting message {} {}", metric.getMetricCase(), key, exception);
            }
        });
    }

    public RecordMetadata emit(final String key, final Metric metric) {
        try {
            return future(key, metric).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CanaryException(e);
        }
    }

    @Override
    public void close() {
        producer.close();
    }
}
