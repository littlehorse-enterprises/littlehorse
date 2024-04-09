package io.littlehorse.canary.util;

import io.littlehorse.canary.proto.MetricKey;
import io.littlehorse.canary.proto.MetricValue;
import io.littlehorse.canary.proto.Tag;

import java.util.List;

public class MetricFactory {

    private MetricFactory() {}

    public static MetricKey buildKey(
            final String id, final String serverHost, final Integer serverPort, final String serverVersion) {
        return buildKey(id, serverHost, serverPort, serverVersion, null);
    }

    public static MetricKey buildKey(
            final String id,
            final String serverHost,
            final int serverPort,
            final String serverVersion,
            final List<Tag> extraTags) {
        return MetricKey.newBuilder()
                .addTags(Tag.newBuilder().setKey("server").setValue("%s:%s".formatted(serverHost, serverPort)))
                .addTags(Tag.newBuilder().setKey("server_version").setValue(serverVersion))
                .addAllTags(extraTags != null ? extraTags : List.of())
                .setId("canary_%s".formatted(id))
                .build();
    }

    public static MetricValue buildValue(final double value) {
        return MetricValue.newBuilder().setValue(value).build();
    }
}
