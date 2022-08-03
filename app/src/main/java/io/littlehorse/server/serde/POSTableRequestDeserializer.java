package io.littlehorse.server.serde;

import org.apache.kafka.common.serialization.Deserializer;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.server.model.internal.POSTableRequest;

public class POSTableRequestDeserializer
implements Deserializer<POSTableRequest> {
    public POSTableRequest deserialize(String topic, byte[] data) {
        try {
            return LHSerializable.fromBytes(data, POSTableRequest.class);
        } catch(LHSerdeError exn) {
            throw new RuntimeException(exn);
        }
    }
}
