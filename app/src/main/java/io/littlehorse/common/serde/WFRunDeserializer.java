package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.run.WFRun;
import io.littlehorse.common.proto.WFRunPb;

public class WFRunDeserializer implements Deserializer<WFRun> {
    public WFRun deserialize(String topic, byte[] data) {
        try {
            return WFRun.fromProto(WFRunPb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }
}
