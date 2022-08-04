package io.littlehorse.common.util.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.LHSerializable;

public class LHSerde<T extends LHSerializable<?>> implements Serde<T> {
    private Serializer<T> ser;
    private Deserializer<T> deser;

    public Serializer<T> serializer() {
        return ser;
    }

    public Deserializer<T> deserializer() {
        return deser;
    }

    public LHSerde(Class<T> cls) {
        this.ser = new LHSerializer<T>();
        this.deser = new LHDeserializer<T>(cls);
    }
}
