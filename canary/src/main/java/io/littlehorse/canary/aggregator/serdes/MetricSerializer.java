package io.littlehorse.canary.aggregator.serdes;

import io.littlehorse.canary.proto.Metric;
import org.apache.kafka.common.serialization.Serializer;

public class MetricSerializer implements Serializer<Metric> {
    @Override
    public byte[] serialize(final String topic, final Metric data) {
        if (data == null) {
            return null;
        }
        return data.toByteArray();
    }
}
