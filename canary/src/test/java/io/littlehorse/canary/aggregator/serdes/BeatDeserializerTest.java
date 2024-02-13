package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.LatencyBeat;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class BeatDeserializerTest {

    Faker faker = new Faker();

    @Test
    void returnNullIfReceivesNull() {
        BeatDeserializer deserializer = new BeatDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        BeatDeserializer deserializer = new BeatDeserializer();

        Beat expected = Beat.newBuilder()
                .setLatencyBeat(
                        LatencyBeat.newBuilder().setLatency(faker.number().randomNumber()))
                .build();

        byte[] input = expected.toByteArray();

        Beat actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
