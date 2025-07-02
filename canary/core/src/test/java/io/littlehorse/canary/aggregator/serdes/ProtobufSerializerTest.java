package io.littlehorse.canary.aggregator.serdes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.BeatValue;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class ProtobufSerializerTest {

    Faker faker = new Faker();

    @Test
    void shouldReturnNullInCaseOfNull() {
        ProtobufSerializer serializer = new ProtobufSerializer();
        assertNull(serializer.serialize(null, null));
    }

    @Test
    void shouldSerializeProtobufMessage() {
        ProtobufSerializer serializer = new ProtobufSerializer();
        BeatValue metric = BeatValue.newBuilder().setTime(Timestamps.now()).build();

        byte[] expected = metric.toByteArray();
        byte[] actual = serializer.serialize(null, metric);

        assertThat(actual).isEqualTo(expected);
    }
}
