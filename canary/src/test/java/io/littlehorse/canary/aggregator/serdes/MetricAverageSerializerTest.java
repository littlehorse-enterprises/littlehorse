package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.MetricAverage;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class MetricAverageSerializerTest {

    Faker faker = new Faker();

    @Test
    void returnNullInCaseOfNull() {
        MetricAverageSerializer serializer = new MetricAverageSerializer();
        assertNull(serializer.serialize(null, null));
    }

    @Test
    void rightSerialization() {
        MetricAverageSerializer serializer = new MetricAverageSerializer();
        MetricAverage metric = MetricAverage.newBuilder()
                .setAvg(faker.number().randomNumber())
                .setCount(faker.number().randomNumber())
                .setAvg(faker.number().randomNumber())
                .build();

        byte[] expected = metric.toByteArray();
        byte[] actual = serializer.serialize(null, metric);

        assertThat(actual).isEqualTo(expected);
    }
}
