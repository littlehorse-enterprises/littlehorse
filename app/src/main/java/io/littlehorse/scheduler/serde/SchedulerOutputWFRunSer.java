package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.LHConfig;
import io.littlehorse.scheduler.SchedulerOutput;

public class SchedulerOutputWFRunSer implements Serializer<SchedulerOutput> {
    private LHConfig config;

    public SchedulerOutputWFRunSer(LHConfig config) {
        this.config = config;
    }

    public byte[] serialize(String topic, SchedulerOutput input) {
        return input.observabilityEvents.toBytes(config);
    }
}
