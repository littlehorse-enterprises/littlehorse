package io.littlehorse.examples;

import org.apache.kafka.common.serialization.Deserializer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import io.littlehorse.sdk.common.proto.MetadataOutputTopicRecord;
import io.littlehorse.sdk.common.proto.OutputTopicRecord;

public class OutputTopicRecordDeserializer implements Deserializer<Message> {

    @Override
    public Message deserialize(String topic, byte[] data) {
        if (topic.contains("execution")) {
            try {
                return OutputTopicRecord.parseFrom(data);
            } catch(InvalidProtocolBufferException exn) {
                exn.printStackTrace();
                return null;
            }
        } else {
            try {
                return MetadataOutputTopicRecord.parseFrom(data);
            } catch(InvalidProtocolBufferException exn) {
                exn.printStackTrace();
                return null;
            }
        }
    }
}
