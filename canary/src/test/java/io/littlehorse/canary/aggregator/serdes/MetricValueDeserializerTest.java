package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.MetricValue;
import org.junit.jupiter.api.Test;

class MetricValueDeserializerTest {

    @Test
    void returnNullIfReceivesNull() {
        MetricValueDeserializer deserializer = new MetricValueDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        MetricValueDeserializer deserializer = new MetricValueDeserializer();

        MetricValue expected = MetricValue.newBuilder().setValue(10.).build();

        byte[] input = expected.toByteArray();

        MetricValue actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
