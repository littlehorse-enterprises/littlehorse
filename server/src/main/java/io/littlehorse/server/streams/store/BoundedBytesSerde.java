package io.littlehorse.server.streams.store;

import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;

public class BoundedBytesSerde extends Serdes.WrapperSerde<Bytes> {

    public BoundedBytesSerde() {
        super(new LimitedBytesSerializer(), new BytesDeserializer());
    }


    private static class LimitedBytesSerializer extends BytesSerializer {
        @Override
        public byte[] serialize(String topic, Bytes data) {
            byte[] result = super.serialize(topic, data);
            if (result.length > 1024 * 1024) {
                throw new IllegalArgumentException("Data too large");
            }
            return result;
        }
    }
}
