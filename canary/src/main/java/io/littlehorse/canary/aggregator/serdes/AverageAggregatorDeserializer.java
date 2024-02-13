package io.littlehorse.canary.aggregator.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.aggregator.internal.ProtobufDeserializationException;
import io.littlehorse.canary.proto.AverageAggregator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class AverageAggregatorDeserializer implements Deserializer<AverageAggregator> {

    @Override
    public AverageAggregator deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return AverageAggregator.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new ProtobufDeserializationException(e);
        }
    }
}
