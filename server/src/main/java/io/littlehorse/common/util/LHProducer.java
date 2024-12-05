package io.littlehorse.common.util;

import io.littlehorse.common.model.AbstractCommand;
import java.io.Closeable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.utils.Bytes;

public class LHProducer implements Closeable {

    private final Producer<String, Bytes> prod;

    public LHProducer(Properties configs) {
        prod = new KafkaProducer<>(configs);
    }

    public LHProducer(Producer<String, Bytes> prod) {
        this.prod = prod;
    }

    public Future<RecordMetadata> send(String key, AbstractCommand<?> t, String topic, Callback cb, Header... headers) {
        return sendRecord(new ProducerRecord<>(topic, null, key, new Bytes(t.toBytes()), List.of(headers)), cb);
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
