package io.littlehorse.server.serde;

import org.apache.kafka.common.serialization.Deserializer;
import io.littlehorse.common.proto.POSTableResponsePb;

public class POSTableResponsePbDeserializer
implements Deserializer<POSTableResponsePb> {
    public POSTableResponsePb deserialize(String topic, byte[] data) {
        try {
            return POSTableResponsePb.parseFrom(data);
        } catch(Exception exn) {
            throw new RuntimeException(exn);
        }
    }
    
}
