package io.littlehorse.canary.metronome.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.proto.Attempt;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalRepositoryTest {

    private LocalRepository repository;

    @BeforeEach
    void beforeEach() throws IOException {
        repository = new LocalRepository(
                Files.createTempDirectory("canaryRocksDBUnitTests").toString());
    }

    @Test
    void shouldSaveAttemptRecord() {
        String id = UUID.randomUUID().toString();
        Attempt attempt =
                Attempt.newBuilder().setAttempt(9).setStart(Timestamps.now()).build();

        repository.save(id, attempt);

        assertThat(repository.get(id)).isEqualTo(attempt);
    }
}
