package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.scheduler.model.WfRunState;

public class WFRunSerializer implements Serializer<WfRunState> {
    public byte[] serialize(String topic, WfRunState wfRun) {
        return wfRun.toProtoBuilder().build().toByteArray();
    }
}
