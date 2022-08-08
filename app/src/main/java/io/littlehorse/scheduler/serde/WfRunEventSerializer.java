package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.proto.scheduler.WfRunEventPb;

public class WfRunEventSerializer implements Serializer<WfRunEvent> {
    public byte[] serialize(String topic, WfRunEvent evt) {
        WfRunEventPb proto = evt.toProtoBuilder().build();
        return proto.toByteArray();
    }
}
