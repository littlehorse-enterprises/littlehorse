package io.littlehorse.common.util.serde;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class LHDeserializer<T extends LHSerializable<?>> implements Deserializer<T> {

    private Class<T> cls;

    // When we do encryption, we'll need to inject the LHConfig object for
    // access to the encryption keys.
    public LHDeserializer(Class<T> cls) {
        this.cls = cls;
    }

    public T deserialize(String topic, byte[] b) {
        if (b == null) return null;
        try {
            return LHSerializable.fromBytes(b, cls, null); // TODO eduwer
        } catch (LHSerdeException exn) {
            log.error("Caught and re-throwing exception from deserializer.", exn);
            throw new RuntimeException(exn);
        }
    }
}
