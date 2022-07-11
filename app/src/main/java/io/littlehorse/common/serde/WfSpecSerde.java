package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.meta.WfSpec;

public class WfSpecSerde implements Serde<WfSpec> {
    private Deserializer<WfSpec> d;
    private Serializer<WfSpec> s;

    public WfSpecSerde() {
        d = new WfSpecDeserializer();
        s = new WfSpecSerializer();
    }

    public Serializer<WfSpec> serializer() {
        return s;
    }

    public Deserializer<WfSpec> deserializer() {
        return d;
    }
}
