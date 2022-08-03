package io.littlehorse.common.util.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;

public class LHDeserializer<
    U extends MessageOrBuilder, T extends LHSerializable<U>
> implements Deserializer<T> {
    private Class<T> cls;

    public LHDeserializer(Class<T> cls) {
        this.cls = cls;
    }

    public T deserialize(String topic, byte[] b) {
        try {
            return LHSerializable.fromBytes(b, cls);
        } catch(LHSerdeError exn) {
            return null;
        }
    }
}
