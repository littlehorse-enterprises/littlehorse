package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.MetricAverage;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class MetricAverageDeserializerTest {

    Faker faker = new Faker();

    @Test
    void returnNullIfReceivesNull() {
        MetricAverageDeserializer deserializer = new MetricAverageDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        MetricAverageDeserializer deserializer = new MetricAverageDeserializer();

        MetricAverage expected = MetricAverage.newBuilder()
                .setAvg(faker.number().randomNumber())
                .setCount(faker.number().randomNumber())
                .setAvg(faker.number().randomNumber())
                .build();

        byte[] input = expected.toByteArray();

        MetricAverage actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
