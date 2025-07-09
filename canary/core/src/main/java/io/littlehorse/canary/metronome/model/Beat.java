package io.littlehorse.canary.metronome.model;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.proto.BeatValue;
import java.time.Duration;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Beat {
    @NonNull
    private BeatType type;

    private String id;
    private BeatStatus status;
    private Duration latency;
    private Instant time;

    public static BeatBuilder builder(final BeatType type) {
        return new BeatBuilder().type(type);
    }

    public BeatValue toBeatValue() {
        final BeatValue.Builder builder = BeatValue.newBuilder().setTime(Timestamps.now());

        if (getLatency() != null) {
            builder.setLatency(getLatency().toMillis());
        }

        if (getTime() != null) {
            builder.setTime(Timestamps.fromMillis(getTime().toEpochMilli()));
        }

        return builder.build();
    }

    public BeatKey toBeatKey() {
        final BeatKey.Builder builder = BeatKey.newBuilder().setType(type);

        if (id != null) {
            builder.setId(id);
        }

        if (status != null) {
            builder.addAllTags(status.toTags());
        }

        return builder.build();
    }
}
