package io.littlehorse.server.model;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.server.model.internal.POSTableRequest;

public class POSTableRequestSerializer implements Serializer<POSTableRequest> {
    public byte[] serialize(String topic, POSTableRequest req) {
        return req.toBytes();
    }
    
}
