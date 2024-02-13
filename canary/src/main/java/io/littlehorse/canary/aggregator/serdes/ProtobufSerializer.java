package io.littlehorse.canary.aggregator.serdes;

import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.Serializer;

public class ProtobufSerializer<P extends Message> implements Serializer<P> {
    @Override
    public byte[] serialize(final String topic, final P data) {
        if (data == null) {
            return null;
        }
        return data.toByteArray();
    }
}
