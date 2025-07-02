package io.littlehorse.canary.aggregator.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.aggregator.internal.ProtobufDeserializationException;
import io.littlehorse.canary.proto.MetricValue;
import org.apache.kafka.common.serialization.Deserializer;

public class MetricValueDeserializer implements Deserializer<MetricValue> {

    @Override
    public MetricValue deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return MetricValue.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new ProtobufDeserializationException(e);
        }
    }
}
