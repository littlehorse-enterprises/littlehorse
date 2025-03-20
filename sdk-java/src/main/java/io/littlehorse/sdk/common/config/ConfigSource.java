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

    public ConfigSource loadFromMap(Map<?, ?> map) {
        props.putAll(map);
        return this;
    }

    public ConfigSource loadFromConfigSource(ConfigSource configSource) {
        return loadFromProperties(configSource.toProperties());
    }

    public ConfigSource loadFromProperties(Properties properties) {
        return loadFromMap(properties);
    }

    public ConfigSource loadFromPropertiesFile(Path path) {
        return loadFromPropertiesFile(path.toFile());
    }

    public ConfigSource loadFromPropertiesFile(String path) {
        return loadFromPropertiesFile(Path.of(path));
    }

    public ConfigSource loadFromPropertiesFile(File file) {
        log.info("Loading config from {}", file);
        try {
            props.load(new FileReader(file));
        } catch (IOException e) {
            throw new ConfigurationFileException(e);
        }
        return this;
    }

    public ConfigSource loadFromEnvVariables() {
        log.info("Loading config from environment variables");
        props.putAll(System.getenv());
        return this;
    }
}
