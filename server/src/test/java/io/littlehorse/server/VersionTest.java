package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.ServerVersionResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    void testDevelopmentVersion() {

        Assertions.assertThat(ServerVersion.VERSION).isNotNull();

        ServerVersionResponse serverVersionResponse = Version.getServerVersion();

        String version = String.format(
                "%s.%s.%s",
                serverVersionResponse.getMajorVersion(),
                serverVersionResponse.getMinorVersion(),
                serverVersionResponse.getPatchVersion());

        Assertions.assertThat(
                        serverVersionResponse.getPreReleaseIdentifier().isEmpty()
                                ? version
                                : String.format("%s-%s", version, serverVersionResponse.getPreReleaseIdentifier()))
                .isEqualTo(ServerVersion.VERSION);
    }
}
