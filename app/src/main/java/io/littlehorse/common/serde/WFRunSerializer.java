package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.scheduler.WfRun;

public class WFRunSerializer implements Serializer<WfRun> {
    public byte[] serialize(String topic, WfRun wfRun) {
        return wfRun.toProtoBuilder().build().toByteArray();
    }
}
