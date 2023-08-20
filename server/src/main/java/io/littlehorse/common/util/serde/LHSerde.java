package io.littlehorse.common.util.serde;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class LHSerde<T extends LHSerializable<?>> implements Serde<T> {

    private Serializer<T> ser;
    private Deserializer<T> deser;

    public Serializer<T> serializer() {
        return ser;
    }

    public Deserializer<T> deserializer() {
        return deser;
    }

    // When we do encryption, we'll need to inject the LHConfig object for
    // access to the encryption keys.
    public LHSerde(Class<T> cls, LHConfig config) {
        this.ser = new LHSerializer<T>(config);
        this.deser = new LHDeserializer<T>(cls, config);
    }
}
