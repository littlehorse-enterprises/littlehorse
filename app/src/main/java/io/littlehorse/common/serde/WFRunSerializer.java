package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.run.WFRun;

public class WFRunSerializer implements Serializer<WFRun> {
    public byte[] serialize(String topic, WFRun wfRun) {
        return wfRun.toProtoBuilder().build().toByteArray();
    }
}
