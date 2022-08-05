package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.wfspec.WFSpecPb;

public class WfSpecSerializer implements Serializer<WfSpec> {
    public byte[] serialize(String topic, WfSpec evt) {
        WFSpecPb proto = evt.toProto().build();
        return proto.toByteArray();
    }
}
