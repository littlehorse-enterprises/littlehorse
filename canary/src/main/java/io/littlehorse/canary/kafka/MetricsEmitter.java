package io.littlehorse.canary.kafka;

import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.prometheus.Measurable;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.util.Shutdown;
import io.micrometer.core.instrument.MeterRegistry;
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
public class MetricsEmitter implements Measurable {

    private final Producer<String, Bytes> producer;
    private final String topicName;
    private final KafkaClientMetrics kafkaClientMetrics;

    public MetricsEmitter(final String topicName, final Map<String, Object> kafkaProducerConfigMap) {
        this.topicName = topicName;

        producer = new KafkaProducer<>(kafkaProducerConfigMap);
        Shutdown.addShutdownHook(producer);

        kafkaClientMetrics = new KafkaClientMetrics(producer);
        Shutdown.addShutdownHook(kafkaClientMetrics);
    }

    /**
     * Asynchronous method that produces a metric to kafka but does not block the current thread
     * @param key
     * @param metric
     * @return Future<RecordMetadata>
     */
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

    /**
     * Blocking method that produces a metric and waits until kafka acknowledges
     * @param key
     * @param metric
     * @return RecordMetadata
     */
    public RecordMetadata emit(final String key, final Metric metric) {
        try {
            return future(key, metric).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CanaryException(e);
        }
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        kafkaClientMetrics.bindTo(registry);
    }
}
