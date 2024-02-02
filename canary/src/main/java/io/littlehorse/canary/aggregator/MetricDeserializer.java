package io.littlehorse.canary.aggregator;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.proto.Metric;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class MetricDeserializer implements Deserializer<Metric> {

    @Override
    public Metric deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return Metric.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            log.error("Error in stream topology {}", e.getMessage(), e);
            throw new CanaryException(e);
        }
    }
}
