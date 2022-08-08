package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.proto.scheduler.WFRunEventPb;

public class WFRunEventSerializer implements Serializer<WfRunEvent> {
    public byte[] serialize(String topic, WfRunEvent evt) {
        WFRunEventPb proto = evt.toProtoBuilder().build();
        return proto.toByteArray();
    }
}
