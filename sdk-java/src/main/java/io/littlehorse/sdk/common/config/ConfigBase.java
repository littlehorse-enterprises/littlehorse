package io.littlehorse.sdk.common.config;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConfigBase {

    protected Properties props;

    public ConfigBase(ConfigSource configSource) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromConfigSource(configSource)
                .toProperties();
    }

    public ConfigBase(String propLocation) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromPropertiesFile(propLocation)
                .toProperties();
    }

    public ConfigBase(Path propLocation) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromPropertiesFile(propLocation)
                .toProperties();
    }

    public ConfigBase(Properties props) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromProperties(props)
                .toProperties();
    }

    public ConfigBase(Map<String, Object> configs) {
        this.props =
                ConfigSource.newSource(getEnvKeyPrefixes()).loadFromMap(configs).toProperties();
    }

    public ConfigBase() {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromEnvVariables()
                .toProperties();
    }

    protected abstract String[] getEnvKeyPrefixes();

    protected String getOrSetDefault(String key, String defaultVal) {
        if (!props.containsKey(key)) {
            if (defaultVal != null) {
                log.info("Config: {} unset, defaulting to: {}", key, defaultVal);
                props.setProperty(key, defaultVal);
            }
            return defaultVal;
        } else {
            return String.valueOf(props.get(key));
        }
    }
}
