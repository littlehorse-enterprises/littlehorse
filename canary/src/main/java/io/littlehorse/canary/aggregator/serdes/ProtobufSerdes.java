package io.littlehorse.canary.aggregator.serdes;

import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.MetricAverage;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class ProtobufSerdes {
    private ProtobufSerdes() {}

    public static Serde<Metric> Metric() {
        return Serdes.serdeFrom(new MetricSerializer(), new MetricDeserializer());
    }

    public static Serde<MetricAverage> MetricAverage() {
        return Serdes.serdeFrom(new MetricAverageSerializer(), new MetricAverageDeserializer());
    }
}
