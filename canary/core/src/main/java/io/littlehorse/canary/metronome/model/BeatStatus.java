package io.littlehorse.canary.metronome.model;

import io.littlehorse.canary.proto.Tag;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class BeatStatus {

    private static final String STATUS = "status";
    private static final String REASON = "reason";
    private static final String DEFAULT_REASON = "";

    @NonNull
    private Code code;

    private Source source;
    private String reason;

    public static BeatStatusBuilder builder(final Code code) {
        return new BeatStatusBuilder().code(code).source(Source.CANARY);
    }

    public List<Tag> toTags() {
        final Tag statusTag = Tag.newBuilder()
                .setKey(STATUS)
                .setValue(code.name().toLowerCase())
                .build();
        final Tag reasonTag =
                Tag.newBuilder().setKey(REASON).setValue(getReason()).build();

        return List.of(statusTag, reasonTag);
    }

    private String getReason() {
        return reason == null
                ? DEFAULT_REASON
                : "%s_%s"
                        .formatted(source.name(), reason.replaceAll("([a-z])([A-Z]+)", "$1_$2"))
                        .toLowerCase();
    }

    public enum Code {
        OK,
        ERROR
    }

    public enum Source {
        CANARY,
        GRPC,
        WORKFLOW
    }
}
