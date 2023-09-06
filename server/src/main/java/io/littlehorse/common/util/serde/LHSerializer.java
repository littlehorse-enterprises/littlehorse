package io.littlehorse.common.util.serde;

import io.littlehorse.common.LHSerializable;
import org.apache.kafka.common.serialization.Serializer;

public class LHSerializer<T extends LHSerializable<?>> implements Serializer<T> {

    // When we do encryption, we'll need to inject the LHConfig object for
    // access to the encryption keys.
    public LHSerializer() {}

    public byte[] serialize(String topic, T t) {
        if (t == null) return null;
        return t.toBytes();
    }
}
