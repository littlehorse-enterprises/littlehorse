package io.littlehorse.examples;

import org.apache.kafka.common.serialization.Deserializer;

import com.google.protobuf.InvalidProtocolBufferException;

import io.littlehorse.sdk.common.proto.OutputTopicRecord;

public class OutputTopicRecordDeserializer implements Deserializer<OutputTopicRecord> {

    @Override
    public OutputTopicRecord deserialize(String topic, byte[] data) {
        try {
            return OutputTopicRecord.parseFrom(data);
        } catch(InvalidProtocolBufferException exn) {
            exn.printStackTrace();
            return null;
        }
    }
    
}
