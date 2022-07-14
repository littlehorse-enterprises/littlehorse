package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.run.WfRun;
import io.littlehorse.common.proto.WFRunPb;

public class WFRunDeserializer implements Deserializer<WfRun> {
    public WfRun deserialize(String topic, byte[] data) {
        try {
            return WfRun.fromProto(WFRunPb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }
}
