package io.littlehorse.canary.aggregator.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.aggregator.internal.ProtobufDeserializationException;
import io.littlehorse.canary.proto.MetricAverage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class MetricAverageDeserializer implements Deserializer<MetricAverage> {

    @Override
    public MetricAverage deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return MetricAverage.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new ProtobufDeserializationException(e);
        }
    }
}
