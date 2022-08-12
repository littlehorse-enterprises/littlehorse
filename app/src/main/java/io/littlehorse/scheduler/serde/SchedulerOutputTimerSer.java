package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.LHConfig;
import io.littlehorse.scheduler.SchedulerOutput;

public class SchedulerOutputTimerSer implements Serializer<SchedulerOutput> {
    private LHConfig config;

    public SchedulerOutputTimerSer(LHConfig config) {
        this.config = config;
    }

    public byte[] serialize(String topic, SchedulerOutput input) {
        return input.timer.toBytes(config);
    }
}
