package io.littlehorse.common.util;

import io.littlehorse.common.model.AbstractCommand;
import java.io.Closeable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.utils.Bytes;

public class LHProducer implements Closeable {

    private KafkaProducer<String, Bytes> prod;

    public LHProducer(Properties config) {
        prod = new KafkaProducer<>(config);
    }

    public Future<RecordMetadata> send(String key, AbstractCommand<?> t, String topic, Callback cb, Header... headers) {
        return sendRecord(new ProducerRecord<>(topic, null, key, new Bytes(t.toBytes()), List.of(headers)), cb);
    }

    public Future<RecordMetadata> send(String key, AbstractCommand<?> t, String topic) {
        return this.send(key, t, topic, null);
    }

    public Future<RecordMetadata> sendRecord(ProducerRecord<String, Bytes> record, Callback cb) {
        return (cb != null) ? prod.send(record, cb) : prod.send(record);
    }

    public Future<RecordMetadata> sendToPartition(String key, AbstractCommand<?> val, String topic, int partition) {
        Bytes valBytes = val == null ? null : new Bytes(val.toBytes());
        return sendRecord(new ProducerRecord<>(topic, partition, key, valBytes), null);
    }

    public void close() {
        this.prod.close();
    }
}
