package io.littlehorse.common.util;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractCommand;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.utils.Bytes;

public class LHProducer implements Closeable {

    private KafkaProducer<String, Bytes> prod;

    public LHProducer(LHServerConfig config) {
        prod = new KafkaProducer<>(config.getKafkaProducerConfig(config.getLHInstanceName()));
    }

    public Future<RecordMetadata> send(String key, AbstractCommand<?> t, String topic, Callback cb, Header... headers) {
        return doSend(new ProducerRecord<>(topic, null, key, new Bytes(t.toBytes()), List.of(headers)), cb);
    }

    private Future<RecordMetadata> doSend(ProducerRecord<String, Bytes> record, Callback cb) {
        return (cb != null) ? prod.send(record, cb) : prod.send(record);
    }

    public void close() {
        this.prod.close();
    }
}
