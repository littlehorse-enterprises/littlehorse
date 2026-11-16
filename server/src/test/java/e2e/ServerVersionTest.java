package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.LittleHorseVersion;
import io.littlehorse.server.Version;
import io.littlehorse.test.LHTest;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@LHTest
public class ServerVersionTest {
    private LittleHorseBlockingStub client;

    @Test
    void shouldReturnConsistentVersionForConcurrentRequests() throws Exception {
        LittleHorseVersion expected = Version.getCurrentServerVersion();
        assertThat(expected).isNotNull();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<Future<?>>();
            for (int worker = 0; worker < 8; worker++) {
                results.add(executor.submit(() -> {
                    start.await();
                    for (int iteration = 0; iteration < 100; iteration++) {
                        assertThat(client.withDeadlineAfter(5, TimeUnit.SECONDS)
                                        .getServerVersion(Empty.getDefaultInstance()))
                                .isEqualTo(expected);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get(30, TimeUnit.SECONDS);
            }
        }
    }
}
