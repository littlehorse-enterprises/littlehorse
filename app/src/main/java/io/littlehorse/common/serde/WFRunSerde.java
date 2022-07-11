package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.run.WFRun;

public class WFRunSerde implements Serde<WFRun> {
    private Deserializer<WFRun> d;
    private Serializer<WFRun> s;

    public WFRunSerde() {
        d = new WFRunDeserializer();
        s = new WFRunSerializer();
    }

    public Serializer<WFRun> serializer() {
        return s;
    }

    public Deserializer<WFRun> deserializer() {
        return d;
    }
}
