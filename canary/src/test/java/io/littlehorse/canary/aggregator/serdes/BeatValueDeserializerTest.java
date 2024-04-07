package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.LatencyBeat;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class BeatValueDeserializerTest {

    Faker faker = new Faker();

    @Test
    void returnNullIfReceivesNull() {
        BeatValueDeserializer deserializer = new BeatValueDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        BeatValueDeserializer deserializer = new BeatValueDeserializer();

        BeatValue expected = BeatValue.newBuilder()
                .setLatencyBeat(
                        LatencyBeat.newBuilder().setLatency(faker.number().randomNumber()))
                .build();

        byte[] input = expected.toByteArray();

        BeatValue actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
