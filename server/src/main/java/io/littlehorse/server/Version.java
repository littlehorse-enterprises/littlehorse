package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.ServerVersionResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private static final Pattern pattern =
            Pattern.compile("(?<major>0|[1-9]\\d*)\\.(?<minor>\\d+)\\.(?<patch>\\d+)(?:-(?<prerelease>[a-zA-Z0-9]+))?");
    private static final Matcher matcher = pattern.matcher(ServerVersion.VERSION);

    public static ServerVersionResponse getServerVersion() {
        if (matcher.matches()) {
            ServerVersionResponse.Builder builder = ServerVersionResponse.newBuilder()
                    .setMajorVersion(Integer.parseInt(matcher.group("major")))
                    .setMinorVersion(Integer.parseInt(matcher.group("minor")))
                    .setPatchVersion(Integer.parseInt(matcher.group("patch")));

            if (matcher.group("prerelease") != null) {
                builder.setPreReleaseIdentifier(matcher.group("prerelease"));
            }

            return builder.build();
        }
        return null;
    }
}
