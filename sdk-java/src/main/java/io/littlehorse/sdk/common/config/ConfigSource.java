package io.littlehorse.sdk.common.config;

import io.littlehorse.sdk.common.exception.ConfigurationFileException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Class helper to load properties from different sources
 */
@Slf4j
public class ConfigSource {

    private final Properties props;
    private final List<String> prefixes;

    private ConfigSource(Properties props, List<String> prefixes) {
        this.props = props;
        this.prefixes = prefixes;
    }

    /**
     * Creates a new ConfigSource
     * @param prefixes to filter the collection of properties
     * @return this
     */
    public static ConfigSource newSource(String... prefixes) {
        return new ConfigSource(new Properties(), List.of(prefixes));
    }

    /**
     * Returns the currently loaded properties filtered by configured prefixes.
     *
     * @return filtered properties
     */
    public Properties toProperties() {
        Properties newProps = new Properties();
        newProps.putAll(props.entrySet().stream()
                .filter(entry -> isValid(entry.getKey().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return newProps;
    }

    private boolean isValid(String property) {
        return prefixes.isEmpty() || prefixes.stream().anyMatch(property::startsWith);
    }

    /**
     * Merges key/value pairs into this source.
     *
     * @param map values to merge
     * @return this source
     */
    public ConfigSource loadFromMap(Map<?, ?> map) {
        props.putAll(map);
        return this;
    }

    /**
     * Loads properties from another source into this source.
     *
     * @param configSource source whose filtered properties are merged into this source
     * @return this source
     */
    public ConfigSource loadFromConfigSource(ConfigSource configSource) {
        return loadFromProperties(configSource.toProperties());
    }

    /**
     * Merges Java properties into this source.
     *
     * @param properties properties to merge
     * @return this source
     */
    public ConfigSource loadFromProperties(Properties properties) {
        return loadFromMap(properties);
    }

    /**
     * Loads properties from a file path.
     *
     * @param path path to a .properties file
     * @return this source
     */
    public ConfigSource loadFromPropertiesFile(Path path) {
        return loadFromPropertiesFile(path.toFile());
    }

    /**
     * Loads properties from a file path string.
     *
     * @param path path to a .properties file
     * @return this source
     */
    public ConfigSource loadFromPropertiesFile(String path) {
        return loadFromPropertiesFile(Path.of(path));
    }

    /**
     * Loads properties from a file.
     *
     * @param file properties file
     * @return this source
     */
    public ConfigSource loadFromPropertiesFile(File file) {
        log.info("Loading config from {}", file);
        try {
            props.load(new FileReader(file));
        } catch (IOException e) {
            throw new ConfigurationFileException(e);
        }
        return this;
    }

    /**
     * Merges current process environment variables into this source.
     *
     * @return this source
     */
    public ConfigSource loadFromEnvVariables() {
        log.info("Loading config from environment variables");
        props.putAll(System.getenv());
        return this;
    }
}
