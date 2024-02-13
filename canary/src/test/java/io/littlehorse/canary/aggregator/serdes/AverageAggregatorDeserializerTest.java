package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.AverageAggregator;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class AverageAggregatorDeserializerTest {

    Faker faker = new Faker();

    @Test
    void returnNullIfReceivesNull() {
        AverageAggregatorDeserializer deserializer = new AverageAggregatorDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        AverageAggregatorDeserializer deserializer = new AverageAggregatorDeserializer();

        AverageAggregator expected = AverageAggregator.newBuilder()
                .setAvg(faker.number().randomNumber())
                .setCount(faker.number().randomDigit())
                .setAvg(faker.number().randomNumber())
                .build();

        byte[] input = expected.toByteArray();

        AverageAggregator actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
