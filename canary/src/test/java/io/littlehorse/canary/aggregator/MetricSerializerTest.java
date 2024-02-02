package io.littlehorse.canary.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Metadata;
import io.littlehorse.canary.proto.Metric;
import org.junit.jupiter.api.Test;

class MetricSerializerTest {

    @Test
    void returnNullInCaseOfNull() {
        MetricSerializer serializer = new MetricSerializer();
        assertNull(serializer.serialize(null, null));
    }

    @Test
    void rightSerialization() {
        MetricSerializer serializer = new MetricSerializer();
        Metric metric = Metric.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setTime(Timestamps.now())
                        .setServerVersion("my-version")
                        .setServerHost("my-server"))
                .build();

        byte[] expected = metric.toByteArray();
        byte[] actual = serializer.serialize(null, metric);

        assertThat(actual).isEqualTo(expected);
    }
}
