package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.event.WfRunEvent;

public class WFRunEventSerde implements Serde<WfRunEvent> {
    private Deserializer<WfRunEvent> d;
    private Serializer<WfRunEvent> s;

    public WFRunEventSerde() {
        d = new WFRunEventDeserializer();
        s = new WFRunEventSerializer();
    }

    public Serializer<WfRunEvent> serializer() {
        return s;
    }

    public Deserializer<WfRunEvent> deserializer() {
        return d;
    }
}
