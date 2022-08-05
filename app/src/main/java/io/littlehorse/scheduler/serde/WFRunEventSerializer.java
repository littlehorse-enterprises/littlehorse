package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.proto.scheduler.WFRunEventPb;

public class WFRunEventSerializer implements Serializer<WFRunEvent> {
    public byte[] serialize(String topic, WFRunEvent evt) {
        WFRunEventPb proto = evt.toProtoBuilder().build();
        return proto.toByteArray();
    }
}
