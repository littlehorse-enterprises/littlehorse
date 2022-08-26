package io.littlehorse.server.model.scheduler.util;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.LHConfig;

public class SchedulerOutputTsrSer implements Serializer<SchedulerOutput> {
    private LHConfig config;

    public SchedulerOutputTsrSer(LHConfig config) {
        this.config = config;
    }

    public byte[] serialize(String topic, SchedulerOutput input) {
        return input.request.toBytes(config);
    }
}
