package io.littlehorse.canary.metronome.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Status;
import io.littlehorse.canary.proto.Tag;
import org.junit.jupiter.api.Test;

class BeatStatusTest {

    @Test
    void shouldInitializeStatus() {
        BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.OK).build();
        assertThat(beatStatus.toTags())
                .containsExactly(
                        Tag.newBuilder().setKey("status").setValue("ok").build(),
                        Tag.newBuilder().setKey("reason").build());
    }

    @Test
    void shouldFormatReasonAsSnakeCaseException() {
        BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.ERROR)
                .reason(NullPointerException.class.getSimpleName())
                .build();

        assertThat(beatStatus.toTags())
                .containsExactly(
                        Tag.newBuilder().setKey("status").setValue("error").build(),
                        Tag.newBuilder()
                                .setKey("reason")
                                .setValue("canary_null_pointer_exception")
                                .build());
    }

    @Test
    void shouldFormatReasonForGrpc() {
        BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.ERROR)
                .source(BeatStatus.Source.GRPC)
                .reason(Status.Code.ALREADY_EXISTS.name())
                .build();

        assertThat(beatStatus.toTags())
                .containsExactly(
                        Tag.newBuilder().setKey("status").setValue("error").build(),
                        Tag.newBuilder()
                                .setKey("reason")
                                .setValue("grpc_already_exists")
                                .build());
    }
}
