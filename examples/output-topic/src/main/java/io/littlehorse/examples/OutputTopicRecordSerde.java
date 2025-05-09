package io.littlehorse.examples;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import io.littlehorse.sdk.common.proto.OutputTopicRecord;

public class OutputTopicRecordSerde implements Serde<OutputTopicRecord> {

    @Override
    public Serializer<OutputTopicRecord> serializer() {
        return new OutputTopicRecordSerializer();
    }

    @Override
    public Deserializer<OutputTopicRecord> deserializer() {
        return new OutputTopicRecordDeserializer();
    }
    
}
