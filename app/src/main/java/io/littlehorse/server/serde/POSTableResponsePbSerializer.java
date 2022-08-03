package io.littlehorse.server.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.proto.POSTableResponsePb;

public class POSTableResponsePbSerializer
implements Serializer<POSTableResponsePb> {
    public byte[] serialize(String topic, POSTableResponsePb proto) {
        return proto.toByteArray();
    }
}
