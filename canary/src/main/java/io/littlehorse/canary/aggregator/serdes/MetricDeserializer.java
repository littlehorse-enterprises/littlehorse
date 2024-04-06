package io.littlehorse.canary.aggregator.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.aggregator.internal.ProtobufDeserializationException;
import io.littlehorse.canary.proto.Metric;
import org.apache.kafka.common.serialization.Deserializer;

public class MetricDeserializer implements Deserializer<Metric> {

    @Override
    public Metric deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return Metric.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new ProtobufDeserializationException(e);
        }
    }
}
