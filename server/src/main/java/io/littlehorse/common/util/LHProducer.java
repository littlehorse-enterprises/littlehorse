package io.littlehorse.common.util;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.Command;
import java.io.Closeable;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.utils.Bytes;

public class LHProducer implements Closeable {

    private KafkaProducer<String, Bytes> prod;
    private LHConfig config;

    public LHProducer(LHConfig config) {
        prod = new KafkaProducer<>(config.getKafkaProducerConfig(config.getLHInstanceId()));
        this.config = config;
    }

    public Future<RecordMetadata> send(String key, Command t, String topic, Callback cb) {
        return sendRecord(new ProducerRecord<>(topic, key, new Bytes(t.toBytes(config))), cb);
    }

    public Future<RecordMetadata> send(String key, Command t, String topic) {
        return this.send(key, t, topic, null);
    }

    public Future<RecordMetadata> sendRecord(ProducerRecord<String, Bytes> record, Callback cb) {
        return (cb != null) ? prod.send(record, cb) : prod.send(record);
    }

    public Future<RecordMetadata> sendToPartition(String key, Command val, String topic, int partition) {
        Bytes valBytes = val == null ? null : new Bytes(val.toBytes(config));
        return sendRecord(new ProducerRecord<>(topic, partition, key, valBytes), null);
    }

    public void close() {
        this.prod.close();
    }
}
