package io.littlehorse.sdk.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class LHWorkerConfigTest {

    Faker faker = new Faker();

    @Test
    @SetEnvironmentVariable(key = "LHW_TASK_WORKER_VERSION", value = "v1.0.2")
    void getTaskWorkerVersionFromEnvVariable() {
        LHWorkerConfig config = new LHWorkerConfig();

        String result = config.getTaskWorkerVersion();

        assertEquals("v1.0.2", result);
    }

    @Test
    void setEmptyTaskWorkerVersionIfEnvVariableDoesNotExist() {
        LHWorkerConfig config = new LHWorkerConfig();

        String result = config.getTaskWorkerVersion();

        assertEquals("", result);
    }

    @Test
    void haveAllConfigs() {
        assertThat(LHWorkerConfig.configNames().size()).isEqualTo(3);
    }

    @Test
    void shouldThrowAnExceptionIfTryToModifyConfigNames() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> LHWorkerConfig.configNames().add(faker.regexify("[a-z]{10}"))
        );
    }
}
