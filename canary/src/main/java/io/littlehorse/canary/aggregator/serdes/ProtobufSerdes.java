package io.littlehorse.canary.aggregator.serdes;

import io.littlehorse.canary.proto.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class ProtobufSerdes {
    private ProtobufSerdes() {}

    public static Serde<AverageAggregator> AverageAggregator() {
        return Serdes.serdeFrom(new ProtobufSerializer<>(), new AverageAggregatorDeserializer());
    }

    public static Serde<Beat> Beat() {
        return Serdes.serdeFrom(new ProtobufSerializer<>(), new BeatDeserializer());
    }

    public static Serde<BeatKey> BeatKey() {
        return Serdes.serdeFrom(new ProtobufSerializer<>(), new BeatKeyDeserializer());
    }

    public static Serde<Metric> Metric() {
        return Serdes.serdeFrom(new ProtobufSerializer<>(), new MetricDeserializer());
    }

    public static Serde<MetricKey> MetricKey() {
        return Serdes.serdeFrom(new ProtobufSerializer<>(), new MetricKeyDeserializer());
    }
}
