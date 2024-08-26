package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.ServerVersionResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    void testDevelopmentVersion() {

        Assertions.assertThat(ServerVersion.VERSION).isEqualTo("0.0.0-development");

        Assertions.assertThat(Version.getServerVersion())
                .isEqualTo(ServerVersionResponse.newBuilder()
                        .setMajorVersion(0)
                        .setMinorVersion(0)
                        .setPatchVersion(0)
                        .setPreReleaseIdentifier("development")
                        .build());
    }
}
