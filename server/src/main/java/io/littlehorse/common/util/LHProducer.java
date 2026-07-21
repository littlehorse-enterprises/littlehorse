package io.littlehorse.common.util;

import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.server.streams.store.BoundedBytesSerde;
import java.io.Closeable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public class LHProducer implements Closeable {

    private final Producer<String, byte[]> prod;
    private final BoundedBytesSerde.BoundedBytesSerializer commandSerializer;

    public LHProducer(Properties config, int maxProducerRequestSize) {
        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                Serdes.ByteArray().serializer().getClass());
        this.prod = new KafkaProducer<>(config);
        this.commandSerializer = new BoundedBytesSerde.BoundedBytesSerializer(maxProducerRequestSize);
    }

    // Visible for testing
    public LHProducer(Producer<String, byte[]> prod) {
        this.prod = prod;
        this.commandSerializer = new BoundedBytesSerde.BoundedBytesSerializer(Integer.MAX_VALUE);
    }

    /**
     * Sends a Command asynchronously to the underlying producer without blocking the caller's thread.
     */
    public CompletableFuture<RecordMetadata> send(String key, AbstractCommand<?> t, String topic, Header... headers) {
        try {
            final byte[] valueBytes = commandSerializer.serialize(topic, new Bytes(t.toBytes()));
            return sendRecord(new ProducerRecord<>(topic, null, key, valueBytes, List.of(headers)));
        } catch (RecordTooLargeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends a record asynchronously to the underlying producer without blocking the caller's thread.
     * The task is executed in the ForkJoin pool to ensure non-blocking behavior, and the result
     * is encapsulated in a CompletableFuture for easier chaining and error handling.
     *
     * @param record the record to be sent to the producer
     * @return a CompletableFuture containing the metadata of the sent record or an exception if sending fails
     */
    private CompletableFuture<RecordMetadata> sendRecord(ProducerRecord<String, byte[]> record) {
        CompletableFutureCallback out = new CompletableFutureCallback();
        prod.send(record, out);
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
