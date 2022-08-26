package io.littlehorse.server.model.scheduler.util;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.LHConfig;

public class SchedulerOutputWFRunSer implements Serializer<SchedulerOutput> {
    private LHConfig config;

    public SchedulerOutputWFRunSer(LHConfig config) {
        this.config = config;
    }

    public byte[] serialize(String topic, SchedulerOutput input) {
        return input.observabilityEvents.toBytes(config);
    }
}
