package io.littlehorse.canary.aggregator.serdes;

import io.littlehorse.canary.proto.MetricAverage;
import org.apache.kafka.common.serialization.Serializer;

public class MetricAverageSerializer implements Serializer<MetricAverage> {
    @Override
    public byte[] serialize(final String topic, final MetricAverage data) {
        if (data == null) {
            return null;
        }
        return data.toByteArray();
    }
}
