package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.LittleHorseVersion;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    void testDevelopmentVersion() {

        Assertions.assertThat(ServerVersion.VERSION).isNotNull();

        LittleHorseVersion LittleHorseVersionResponse = Version.getCurrentServerVersion();

        String version = String.format(
                "%s.%s.%s",
                LittleHorseVersionResponse.getMajorVersion(),
                LittleHorseVersionResponse.getMinorVersion(),
                LittleHorseVersionResponse.getPatchVersion());

        Assertions.assertThat(
                        LittleHorseVersionResponse.getPreReleaseIdentifier().isEmpty()
                                ? version
                                : String.format("%s-%s", version, LittleHorseVersionResponse.getPreReleaseIdentifier()))
                .isEqualTo(ServerVersion.VERSION);
    }
}
