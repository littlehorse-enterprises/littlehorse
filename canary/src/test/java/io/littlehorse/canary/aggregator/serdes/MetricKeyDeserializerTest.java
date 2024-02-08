package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.MetricKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MetricKeyDeserializerTest {

    @Test
    void returnNullIfReceivesNull() {
        MetricKeyDeserializer deserializer = new MetricKeyDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        MetricKeyDeserializer deserializer = new MetricKeyDeserializer();

        MetricKey expected =
                MetricKey.newBuilder().setId(UUID.randomUUID().toString()).build();

        byte[] input = expected.toByteArray();

        MetricKey actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
