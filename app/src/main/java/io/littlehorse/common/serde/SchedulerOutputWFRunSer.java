package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.broker.processor.SchedulerOutput;

public class SchedulerOutputWFRunSer implements Serializer<SchedulerOutput> {

    public byte[] serialize(String topic, SchedulerOutput input) {
        return input.observabilityEvents.toProtoBuilder().build().toByteArray();
    }
}
