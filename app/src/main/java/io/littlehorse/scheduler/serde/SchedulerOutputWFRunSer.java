package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.scheduler.SchedulerOutput;

public class SchedulerOutputWFRunSer implements Serializer<SchedulerOutput> {

    public byte[] serialize(String topic, SchedulerOutput input) {
        return input.observabilityEvents.toProtoBuilder().build().toByteArray();
    }
}
