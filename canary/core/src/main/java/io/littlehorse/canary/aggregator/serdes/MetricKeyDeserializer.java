package io.littlehorse.canary.aggregator.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.aggregator.internal.ProtobufDeserializationException;
import io.littlehorse.canary.proto.MetricKey;
import org.apache.kafka.common.serialization.Deserializer;

public class MetricKeyDeserializer implements Deserializer<MetricKey> {

    @Override
    public MetricKey deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return MetricKey.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new ProtobufDeserializationException(e);
        }
    }
}
