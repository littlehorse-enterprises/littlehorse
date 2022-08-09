package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.wfspec.WfSpecPb;

public class WfSpecDeserializer implements Deserializer<WfSpec> {
    public WfSpec deserialize(String topic, byte[] data) {
        try {
            return WfSpec.fromProto(WfSpecPb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }
}
