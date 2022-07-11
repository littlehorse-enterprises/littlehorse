package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.proto.WFRunEventPb;

public class WFRunEventDeserializer implements Deserializer<WFRunEvent> {
    public WFRunEvent deserialize(String topic, byte[] data) {
        try {
            return WFRunEvent.fromProto(WFRunEventPb.parseFrom(data));
        } catch(InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

}
