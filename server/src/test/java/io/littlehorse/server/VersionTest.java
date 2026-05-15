package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.LittleHorseVersion;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    void testDevelopmentVersion() {

        Assertions.assertThat(ServerVersion.VERSION).isNotNull();

        LittleHorseVersion LittleHorseVersionResponse = Version.getCurrentServerVersion();

        final String expectedVersion;
        if (!LittleHorseVersionResponse.hasPatchVersion()) {
            expectedVersion = String.format(
                    "%s.%s",
                    LittleHorseVersionResponse.getMajorVersion(), LittleHorseVersionResponse.getMinorVersion());
        } else {
            expectedVersion = String.format(
                    "%s.%s.%s",
                    LittleHorseVersionResponse.getMajorVersion(),
                    LittleHorseVersionResponse.getMinorVersion(),
                    LittleHorseVersionResponse.getPatchVersion());
        }

        Assertions.assertThat(
                        LittleHorseVersionResponse.getPreReleaseIdentifier().isEmpty()
                                ? expectedVersion
                                : String.format(
                                        "%s-%s", expectedVersion, LittleHorseVersionResponse.getPreReleaseIdentifier()))
                .isEqualTo(ServerVersion.VERSION);
    }
}
