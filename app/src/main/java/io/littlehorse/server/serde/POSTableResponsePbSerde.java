package io.littlehorse.server.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.proto.POSTableResponsePb;

public class POSTableResponsePbSerde implements Serde<POSTableResponsePb> {
    public Serializer<POSTableResponsePb> serializer() {
        return new POSTableResponsePbSerializer();
    }

    public Deserializer<POSTableResponsePb> deserializer() {
        return new POSTableResponsePbDeserializer();
    }
}
