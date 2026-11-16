package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.LittleHorseVersion;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    void shouldReturnConsistentVersionForConcurrentCalls() throws Exception {
        LittleHorseVersion expected = Version.getCurrentServerVersion();
        Assertions.assertThat(expected).isNotNull();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<Future<?>>();
            for (int worker = 0; worker < 8; worker++) {
                results.add(executor.submit(() -> {
                    start.await();
                    for (int iteration = 0; iteration < 1_000; iteration++) {
                        Assertions.assertThat(Version.getCurrentServerVersion()).isEqualTo(expected);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
        }
    }

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
