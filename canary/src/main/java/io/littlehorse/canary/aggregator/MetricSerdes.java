package io.littlehorse.canary.aggregator;

import io.littlehorse.canary.proto.Metric;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class MetricSerdes {
    private MetricSerdes() {}

    public static Serde<Metric> Metric() {
        return Serdes.serdeFrom(new MetricSerializer(), new MetricDeserializer());
    }
}
