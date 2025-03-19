package io.littlehorse.sdk.common.config;

import java.util.List;
import java.util.Properties;

public class ConfigSource {

    private final Properties props;

    private ConfigSource(Properties props) {
        this.props = props;
    }

    public static ConfigSource newSource() {
        return new ConfigSource(new Properties());
    }

    public Properties toProperties() {
        Properties newProps = new Properties();
        newProps.putAll(props);
        return newProps;
    }

    public ConfigSource loadFromEnvVariables(String... prefixes) {
        return loadFromEnvVariables(List.of(prefixes));
    }

    public ConfigSource loadFromEnvVariables(List<String> prefixes) {
        System.getenv().entrySet().stream()
                .filter(entry -> prefixes.isEmpty()
                        || prefixes.stream().anyMatch(prefix -> entry.getKey().startsWith(prefix)))
                .forEach(entry -> props.setProperty(entry.getKey(), entry.getValue()));
        return this;
    }
}
