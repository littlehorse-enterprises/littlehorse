package io.littlehorse.common.util.serde;

import org.apache.kafka.common.serialization.Deserializer;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;

public class LHDeserializer<T extends LHSerializable<?>> implements Deserializer<T> {
    private Class<T> cls;
    private LHConfig config;

    // When we do encryption, we'll need to inject the LHConfig object for
    // access to the encryption keys.
    public LHDeserializer(Class<T> cls, LHConfig config) {
        this.cls = cls;
        this.config = config;
    }

    public T deserialize(String topic, byte[] b) {
        try {
            return LHSerializable.fromBytes(b, cls, config);
        } catch(LHSerdeError exn) {
            return null;
        }
    }
}
