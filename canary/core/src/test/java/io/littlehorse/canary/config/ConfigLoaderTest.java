package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class ConfigLoaderTest {

    public static final String KEY = "lh.canary.test";
    public static final String ENV_VARIABLE_NAME = "LH_CANARY_TEST";
    public static final String EXPECTED_KEY = "test";
    public static final String EXPECTED_FILE_VALUE = "test from file";
    public static final String EXPECTED_ENV_VALUE = "test from env";
    public static final String EXPECTED_PROPERTIES_VALUE = "test from properties";

    @Test
    @ClearEnvironmentVariable(key = ENV_VARIABLE_NAME)
    void shouldLoadConfigurationsFromFile() throws IOException {
        Path configPath = createTemporaryProperties();

        CanaryConfig canaryConfig = ConfigLoader.load(configPath);

        assertThat(canaryConfig.toMap()).contains(entry(EXPECTED_KEY, EXPECTED_FILE_VALUE));
    }

    @Test
    @SetEnvironmentVariable(key = ENV_VARIABLE_NAME, value = EXPECTED_ENV_VALUE)
    void shouldOverwriteFileConfigWithEnvConfigs() throws IOException {
        Path configPath = createTemporaryProperties();

        CanaryConfig canaryConfig = ConfigLoader.load(configPath);

        assertThat(canaryConfig.toMap()).contains(entry(EXPECTED_KEY, EXPECTED_ENV_VALUE));
    }

    @Test
    @ClearEnvironmentVariable(key = ENV_VARIABLE_NAME)
    void shouldLoadFromPropertiesObject() {
        Properties properties = new Properties();
        properties.put(KEY, EXPECTED_PROPERTIES_VALUE);

        CanaryConfig canaryConfig = ConfigLoader.load(properties);

        assertThat(canaryConfig.toMap()).contains(entry(EXPECTED_KEY, EXPECTED_PROPERTIES_VALUE));
    }

    @Test
    @SetEnvironmentVariable(key = ENV_VARIABLE_NAME, value = EXPECTED_ENV_VALUE)
    void shouldOverwritePropertiesWithEnvConfigs() {
        Properties properties = new Properties();
        properties.put(EXPECTED_KEY, EXPECTED_PROPERTIES_VALUE);

        CanaryConfig canaryConfig = ConfigLoader.load(properties);

        assertThat(canaryConfig.toMap()).contains(entry(EXPECTED_KEY, EXPECTED_ENV_VALUE));
    }

    private static Path createTemporaryProperties() throws IOException {
        Path tmpFile = Files.createTempFile("canaryUnitTests", "properties");
        Properties tmpProperties = new Properties();
        tmpProperties.put(KEY, EXPECTED_FILE_VALUE);
        tmpProperties.store(new FileWriter(tmpFile.toFile()), null);
        return tmpFile;
    }
}
