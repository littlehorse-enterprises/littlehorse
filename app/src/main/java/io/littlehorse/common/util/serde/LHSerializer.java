package io.littlehorse.common.util.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.LHSerializable;

public class LHSerializer<T extends LHSerializable<?>> implements Serializer<T> {
    public byte[] serialize(String topic, T t) {
        return t.toBytes();
    }
}
