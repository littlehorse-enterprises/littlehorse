package io.littlehorse.sdk.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class ConfigSourceTest {

    public static final String LHW_TASK_WORKER_VERSION = "LHW_TASK_WORKER_VERSION";
    public static final String RANDOM_VERSION = "v1.0.2";

    @Test
    @SetEnvironmentVariable(key = LHW_TASK_WORKER_VERSION, value = RANDOM_VERSION)
    void shouldLoadEnvVariableWithPrefixes() {
        ConfigSource configSource = ConfigSource.newSource().loadFromEnvVariables("LHW");

        Properties expected = new Properties();
        expected.put(LHW_TASK_WORKER_VERSION, RANDOM_VERSION);
        assertThat(configSource.toProperties()).isEqualTo(expected);
    }

    @Test
    @SetEnvironmentVariable(key = LHW_TASK_WORKER_VERSION, value = RANDOM_VERSION)
    void shouldLoadEnvVariableWithoutPrefixes() {
        ConfigSource configSource = ConfigSource.newSource().loadFromEnvVariables();

        assertThat(configSource.toProperties().size()).isGreaterThan(1);
        assertThat(configSource.toProperties().get(LHW_TASK_WORKER_VERSION)).isEqualTo(RANDOM_VERSION);
    }
}
