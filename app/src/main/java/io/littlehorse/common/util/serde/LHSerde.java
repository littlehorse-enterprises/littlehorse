package io.littlehorse.common.util.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;

public class LHSerde<
    U extends MessageOrBuilder, T extends LHSerializable<U>
> implements Serde<T> {
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
        this.deser = new LHDeserializer<>(cls);
    }
}
