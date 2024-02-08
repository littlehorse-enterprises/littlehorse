package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.LatencyBeat;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class ProtobufSerializerTest {

    Faker faker = new Faker();

    @Test
    void returnNullInCaseOfNull() {
        ProtobufSerializer serializer = new ProtobufSerializer();
        assertNull(serializer.serialize(null, null));
    }

    @Test
    void beatSerialization() {
        ProtobufSerializer serializer = new ProtobufSerializer();
        Beat metric = Beat.newBuilder()
                .setTime(Timestamps.now())
                .setLatencyBeat(
                        LatencyBeat.newBuilder().setLatency(faker.number().randomNumber()))
                .build();

        byte[] expected = metric.toByteArray();
        byte[] actual = serializer.serialize(null, metric);

        assertThat(actual).isEqualTo(expected);
    }
}
