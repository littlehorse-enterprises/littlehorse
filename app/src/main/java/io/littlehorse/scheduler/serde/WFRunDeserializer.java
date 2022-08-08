package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.proto.scheduler.WfRunStatePb;
import io.littlehorse.scheduler.model.WfRunState;

public class WFRunDeserializer implements Deserializer<WfRunState> {
    public WfRunState deserialize(String topic, byte[] data) {
        try {
            return WfRunState.fromProto(WfRunStatePb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }
}
