package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Metadata;
import io.littlehorse.canary.proto.Metric;
import org.junit.jupiter.api.Test;

class MetricDeserializerTest {

    @Test
    void returnNullIfReceivesNull() {
        MetricDeserializer deserializer = new MetricDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        MetricDeserializer deserializer = new MetricDeserializer();

        Metric expected = Metric.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setTime(Timestamps.now())
                        .setServerVersion("my-version")
                        .setServerHost("my-server"))
                .build();

        byte[] input = expected.toByteArray();

        Metric actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
