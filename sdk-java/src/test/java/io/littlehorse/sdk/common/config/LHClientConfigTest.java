package io.littlehorse.sdk.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class LHClientConfigTest {

    Faker faker = new Faker();

    @Test
    void getRandomIdIfItIsUnset() {
        LHConfig config = new LHConfig();

        String result = config.getTaskWorkerId();

        assertThat(result).matches("worker-[a-z0-9]{32}");
    }

    @Test
    void skipSettingRandomIdIfItIsProvided() {
        String expectedClientId = faker.app().name();

        Properties properties = new Properties();
        properties.put(LHConfig.TASK_WORKER_ID_KEY, expectedClientId);

        LHConfig config = new LHConfig(properties);

        String result = config.getTaskWorkerId();

        assertThat(result).isEqualTo(expectedClientId);
    }

    @Test
    @SetEnvironmentVariable(key = "LHW_TASK_WORKER_VERSION", value = "v1.0.2")
    void getTaskWorkerVersionFromEnvVariable() {
        LHConfig config = new LHConfig();

        String result = config.getTaskWorkerVersion();

        assertEquals("v1.0.2", result);
    }

    @Test
    void setEmptyTaskWorkerVersionIfEnvVariableDoesNotExist() {
        LHConfig config = new LHConfig();

        String result = config.getTaskWorkerVersion();

        assertEquals("", result);
    }

    @Test
    void setDefaultProtocol() {
        LHConfig config = new LHConfig();
        assertEquals("PLAINTEXT", config.getApiProtocol());
    }

    @Test
    @SetEnvironmentVariable(key = "LHC_API_PROTOCOL", value = "INVALID")
    void throwsExceptionIfInvalid() {
        LHConfig config = new LHConfig();
        assertThrows(IllegalArgumentException.class, config::getApiProtocol);
    }

    @Test
    void getOnlyOneRandomId() {
        LHConfig config = new LHConfig();

        String result1 = config.getTaskWorkerId();
        String result2 = config.getTaskWorkerId();

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void haveAllConfigs() {
        assertThat(LHConfig.configNames().size()).isEqualTo(LHConfig.ConfigKeys.values().length);
    }

    @Test
    void shouldThrowAnExceptionIfTryToModifyConfigNames() {
        assertThrows(UnsupportedOperationException.class, () -> LHConfig.configNames()
                .add(faker.regexify("[a-z]{10}")));
    }

    @Test
    void shouldExportAllConfigs() {
        List<String> keys =
                Arrays.stream(LHConfig.ConfigKeys.values()).map(Enum::name).collect(Collectors.toList());
        assertThat(LHConfig.configNames()).containsExactlyInAnyOrderElementsOf(keys);
    }
}
