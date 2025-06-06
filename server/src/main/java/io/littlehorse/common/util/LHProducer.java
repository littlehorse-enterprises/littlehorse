package io.littlehorse.common.util;

import io.littlehorse.common.model.AbstractCommand;
import java.io.Closeable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class LHProducer implements Closeable {

    private final Producer<String, Bytes> prod;

    public LHProducer(Properties config) {
        this.prod = new KafkaProducer<>(config);
    }

    public LHProducer(Producer<String, Bytes> prod) {
        this.prod = prod;
    }

    /**
     * Sends a Command asynchronously to the underlying producer without blocking the caller's thread.
     */
    public CompletableFuture<RecordMetadata> send(String key, AbstractCommand<?> t, String topic, Header... headers) {
        return sendRecord(new ProducerRecord<>(topic, null, key, new Bytes(t.toBytes()), List.of(headers)));
    }

    /**
     * Sends a record asynchronously to the underlying producer without blocking the caller's thread.
     * The task is executed in the ForkJoin pool to ensure non-blocking behavior, and the result
     * is encapsulated in a CompletableFuture for easier chaining and error handling.
     *
     * @param record the record to be sent to the producer
     * @return a CompletableFuture containing the metadata of the sent record or an exception if sending fails
     */
    private CompletableFuture<RecordMetadata> sendRecord(ProducerRecord<String, Bytes> record) {
        CompletableFutureCallback out = new CompletableFutureCallback();
        CompletableFuture.runAsync(() -> prod.send(record, out));
        return out;
    }

    public void close() {
        this.prod.close();
    }

    private static class CompletableFutureCallback extends CompletableFuture<RecordMetadata> implements Callback {

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                log.error("Error sending record to Kafka", exception);
                this.completeExceptionally(exception);
            } else {
                log.trace("Command sent {}", metadata);
                this.complete(metadata);
            }
        }
    }
}
