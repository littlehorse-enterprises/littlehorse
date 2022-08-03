package io.littlehorse.server.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.server.model.internal.POSTableRequest;

public class POSTableRequestSerde implements Serde<POSTableRequest> {
    public Serializer<POSTableRequest> serializer() {
        return new POSTableRequestSerializer();
    }

    public Deserializer<POSTableRequest> deserializer() {
        return new POSTableRequestDeserializer();
    }
}
