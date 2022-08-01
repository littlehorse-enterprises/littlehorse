package io.littlehorse.scheduler.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import io.littlehorse.common.model.event.WFRunEvent;

public class WFRunEventSerde implements Serde<WFRunEvent> {
    private Deserializer<WFRunEvent> d;
    private Serializer<WFRunEvent> s;

    public WFRunEventSerde() {
        d = new WFRunEventDeserializer();
        s = new WFRunEventSerializer();
    }

    public Serializer<WFRunEvent> serializer() {
        return s;
    }

    public Deserializer<WFRunEvent> deserializer() {
        return d;
    }
}
