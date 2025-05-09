package io.littlehorse.examples;

import org.apache.kafka.common.serialization.Serializer;

import io.littlehorse.sdk.common.proto.OutputTopicRecord;

public class OutputTopicRecordSerializer implements Serializer<OutputTopicRecord> {

    @Override
    public byte[] serialize(String topic, OutputTopicRecord data) {
        return data.toByteArray();
    }
    
}
