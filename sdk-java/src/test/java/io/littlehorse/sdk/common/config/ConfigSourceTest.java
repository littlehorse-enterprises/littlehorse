package io.littlehorse.sdk.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class ConfigSourceTest {

    public static final String LHW_TASK_WORKER_VERSION = "LHW_TASK_WORKER_VERSION";
    public static final String LHC_API_HOST = "LHC_API_HOST";
    public static final String EXPECTED_VERSION = "v1.0.2";
    public static final String EXPECTED_HOST = "localhost";

    @Test
    @SetEnvironmentVariable(key = LHW_TASK_WORKER_VERSION, value = EXPECTED_VERSION)
    void shouldLoadEnvVariableWithPrefixes() {
        ConfigSource configSource = ConfigSource.newSource("LHW").loadFromEnvVariables();

        Properties expected = new Properties();
        expected.put(LHW_TASK_WORKER_VERSION, EXPECTED_VERSION);
        assertThat(configSource.toProperties()).hasSize(1);
        assertThat(configSource.toProperties()).isEqualTo(expected);
    }

    @Test
    void shouldLoadMapWithPrefixes() {
        ConfigSource configSource =
                ConfigSource.newSource("LHW").loadFromMap(Map.of(LHW_TASK_WORKER_VERSION, EXPECTED_VERSION));

        Properties expected = new Properties();
        expected.put(LHW_TASK_WORKER_VERSION, EXPECTED_VERSION);
        assertThat(configSource.toProperties()).hasSize(1);
        assertThat(configSource.toProperties()).isEqualTo(expected);
    }

    @Test
    void shouldLoadPropertiesWithPrefixes() {
        Properties expected = new Properties();
        expected.put(LHW_TASK_WORKER_VERSION, EXPECTED_VERSION);

        ConfigSource configSource = ConfigSource.newSource("LHW").loadFromMap(expected);

        assertThat(configSource.toProperties()).hasSize(1);
        assertThat(configSource.toProperties()).isEqualTo(expected);
    }

    @Test
    void shouldLoadFileWithPrefixes() throws IOException {
        Properties expected = new Properties();
        expected.put(LHW_TASK_WORKER_VERSION, EXPECTED_VERSION);
        File temporaryFile = Files.newTemporaryFile();
        expected.store(new FileWriter(temporaryFile), "tests");

        ConfigSource configSource = ConfigSource.newSource("LHW").loadFromPropertiesFile(temporaryFile);

        assertThat(configSource.toProperties()).hasSize(1);
        assertThat(configSource.toProperties()).isEqualTo(expected);
    }

    @Test
    @SetEnvironmentVariable(key = LHW_TASK_WORKER_VERSION, value = EXPECTED_VERSION)
    @SetEnvironmentVariable(key = LHC_API_HOST, value = EXPECTED_HOST)
    void shouldFilterWithTwoPrefixes() {
        Properties expected = new Properties();
        expected.put(LHW_TASK_WORKER_VERSION, EXPECTED_VERSION);
        expected.put(LHC_API_HOST, EXPECTED_HOST);

        ConfigSource configSource = ConfigSource.newSource("LHW_", "LHC_").loadFromEnvVariables();

        assertThat(configSource.toProperties()).hasSize(2);
        assertThat(configSource.toProperties()).isEqualTo(expected);
    }

    @Test
    @SetEnvironmentVariable(key = LHW_TASK_WORKER_VERSION, value = EXPECTED_VERSION)
    @SetEnvironmentVariable(key = "RANDOM_VAR", value = "RANDOM_VALUE")
    void shouldLoadEnvVariableWithoutPrefixes() {
        ConfigSource configSource = ConfigSource.newSource().loadFromEnvVariables();

        assertThat(configSource.toProperties().size()).isGreaterThan(1);
        assertThat(configSource.toProperties().get(LHW_TASK_WORKER_VERSION)).isEqualTo(EXPECTED_VERSION);
    }
}
