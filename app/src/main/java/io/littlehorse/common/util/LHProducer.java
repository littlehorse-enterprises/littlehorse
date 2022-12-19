package io.littlehorse.common.util;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Bytes;

public class LHProducer implements Closeable {

    private KafkaProducer<String, Bytes> prod;
    private LHConfig config;
    private boolean transactional;

    public LHProducer(LHConfig config, boolean transactional) {
        this.transactional = transactional;
        if (transactional) {
            prod = new KafkaProducer<>(config.getKafkaTxnProducerConfig());
            prod.initTransactions();
        } else {
            prod = new KafkaProducer<>(config.getKafkaProducerConfig());
        }
        this.config = config;
    }

    public Future<RecordMetadata> send(
        String key,
        LHSerializable<?> t,
        String topic,
        Callback cb
    ) {
        return sendRecord(
            new ProducerRecord<>(topic, key, new Bytes(t.toBytes(config))),
            cb
        );
    }

    public Future<RecordMetadata> send(
        String key,
        LHSerializable<?> t,
        String topic
    ) {
        return this.send(key, t, topic, null);
    }

    public void beginTransaction() {
        if (!transactional) {
            throw new RuntimeException("Tried to begin txn on non-txn producer!");
        }
        prod.beginTransaction();
    }

    public void abortTransaction() {
        if (!transactional) {
            throw new RuntimeException("Tried to begin txn on non-txn producer!");
        }
        prod.abortTransaction();
    }

    public void commitTransaction() {
        if (!transactional) {
            throw new RuntimeException("Tried to begin txn on non-txn producer!");
        }
        prod.commitTransaction();
    }

    public void sendOffsetsToTransaction(
        Map<TopicPartition, OffsetAndMetadata> offsets,
        ConsumerGroupMetadata groupMetadata
    ) {
        if (!transactional) {
            throw new RuntimeException("Tried to begin txn on non-txn producer!");
        }
        prod.sendOffsetsToTransaction(offsets, groupMetadata);
    }

    private Future<RecordMetadata> sendRecord(
        ProducerRecord<String, Bytes> record,
        Callback cb
    ) {
        return (cb != null) ? prod.send(record, cb) : prod.send(record);
    }

    public void close() {
        this.prod.close();
    }
}
