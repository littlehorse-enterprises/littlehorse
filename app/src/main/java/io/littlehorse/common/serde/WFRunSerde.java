package io.littlehorse.common.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.scheduler.WfRun;

public class WFRunSerde implements Serde<WfRun> {
    private Deserializer<WfRun> d;
    private Serializer<WfRun> s;

    public WFRunSerde() {
        d = new WFRunDeserializer();
        s = new WFRunSerializer();
    }

    public Serializer<WfRun> serializer() {
        return s;
    }

    public Deserializer<WfRun> deserializer() {
        return d;
    }
}
