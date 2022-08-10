package io.littlehorse.common.util.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;

public class LHSerializer<T extends LHSerializable<?>> implements Serializer<T> {
    private LHConfig config;

    // When we do encryption, we'll need to inject the LHConfig object for
    // access to the encryption keys.
    public LHSerializer(LHConfig config) {
        this.config = config;
    }
    
    public byte[] serialize(String topic, T t) {
        if (t == null) return null;
        return t.toBytes(config);
    }
}
