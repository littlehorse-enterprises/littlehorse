package io.littlehorse.common.util.serde;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import org.apache.kafka.common.serialization.Deserializer;

public class ProtobufDeserializer<T extends Message> implements Deserializer<T> {

    private final Parser<T> protobufParser;

    public ProtobufDeserializer(final Parser<T> protobufParser) {
        this.protobufParser = protobufParser;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return protobufParser.parseFrom(data);
        } catch (Exception ex) {
            throw new LHSerdeException(ex, "unable to process bytes");
        }
    }
}
