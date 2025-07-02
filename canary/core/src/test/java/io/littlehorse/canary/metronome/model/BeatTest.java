package io.littlehorse.canary.metronome.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.proto.Tag;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BeatTest {

    @Test
    void shouldSetDefaultTimestamp() {
        Beat beat = Beat.builder(BeatType.WF_RUN_REQUEST).build();

        assertThat(beat.toBeatValue().hasTime()).isTrue();
    }

    @Test
    void shouldSetCustomTimestamp() {
        Instant now = Instant.now();
        Beat beat = Beat.builder(BeatType.WF_RUN_REQUEST).time(now).build();

        assertThat(Timestamps.toMillis(beat.toBeatValue().getTime())).isEqualTo(now.toEpochMilli());
    }

    @Test
    void shouldSetStatusTags() {
        BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.ERROR)
                .source(BeatStatus.Source.GRPC)
                .reason(Status.Code.UNAVAILABLE.name())
                .build();
        Beat beat = Beat.builder(BeatType.WF_RUN_REQUEST).status(beatStatus).build();

        assertThat(beat.toBeatKey())
                .isEqualTo(BeatKey.newBuilder()
                        .setType(BeatType.WF_RUN_REQUEST)
                        .addTags(Tag.newBuilder()
                                .setKey("status")
                                .setValue("error")
                                .build())
                        .addTags(Tag.newBuilder()
                                .setKey("reason")
                                .setValue("grpc_unavailable")
                                .build())
                        .build());
    }
}
