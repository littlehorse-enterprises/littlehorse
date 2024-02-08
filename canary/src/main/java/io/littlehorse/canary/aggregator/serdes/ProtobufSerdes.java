package io.littlehorse.canary.aggregator.serdes;

import io.littlehorse.canary.proto.AverageAggregator;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.MetricKey;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class ProtobufSerdes {
    private ProtobufSerdes() {}

    public static Serde<Beat> Beat() {
        return Serdes.serdeFrom(new ProtobufSerializer<Beat>(), new BeatDeserializer());
    }

    public static Serde<AverageAggregator> AverageAggregator() {
        return Serdes.serdeFrom(new ProtobufSerializer<AverageAggregator>(), new AverageAggregatorDeserializer());
    }

    public static Serde<BeatKey> BeatKey() {
        return Serdes.serdeFrom(new ProtobufSerializer<BeatKey>(), new BeatKeyDeserializer());
    }

    public static Serde<MetricKey> MetricKey() {
        return Serdes.serdeFrom(new ProtobufSerializer<MetricKey>(), new MetricKeyDeserializer());
    }
}
