package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.scheduler.model.WfRunState;

public class WfRunSerde implements Serde<WfRunState> {
    private Deserializer<WfRunState> d;
    private Serializer<WfRunState> s;

    public WfRunSerde() {
        d = new WFRunDeserializer();
        s = new WfRunSerializer();
    }

    public Serializer<WfRunState> serializer() {
        return s;
    }

    public Deserializer<WfRunState> deserializer() {
        return d;
    }
}
