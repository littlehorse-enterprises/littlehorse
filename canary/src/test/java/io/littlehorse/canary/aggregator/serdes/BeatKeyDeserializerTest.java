package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.LatencyBeatKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BeatKeyDeserializerTest {

    @Test
    void returnNullIfReceivesNull() {
        BeatKeyDeserializer deserializer = new BeatKeyDeserializer();

        assertNull(deserializer.deserialize(null, null));
    }

    @Test
    void deserialize() {
        BeatKeyDeserializer deserializer = new BeatKeyDeserializer();

        BeatKey expected = BeatKey.newBuilder()
                .setLatencyBeatKey(
                        LatencyBeatKey.newBuilder().setId(UUID.randomUUID().toString()))
                .build();

        byte[] input = expected.toByteArray();

        BeatKey actual = deserializer.deserialize(null, input);

        assertThat(expected).isEqualTo(actual);
    }
}
