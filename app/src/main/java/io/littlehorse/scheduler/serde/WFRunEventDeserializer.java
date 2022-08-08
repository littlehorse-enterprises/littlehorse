package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.proto.scheduler.WFRunEventPb;

public class WFRunEventDeserializer implements Deserializer<WfRunEvent> {
    public WfRunEvent deserialize(String topic, byte[] data) {
        try {
            return WfRunEvent.fromProto(WFRunEventPb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

}
