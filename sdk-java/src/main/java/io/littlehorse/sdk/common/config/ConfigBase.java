package io.littlehorse.sdk.common.config;

import java.nio.file.Path;
import java.util.List;
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
        initialize();
    }

    public ConfigBase(String propLocation) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromPropertiesFile(propLocation)
                .toProperties();
        initialize();
    }

    public ConfigBase(Path propLocation) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromPropertiesFile(propLocation)
                .toProperties();
        initialize();
    }

    public ConfigBase(Properties props) {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromProperties(props)
                .toProperties();
        initialize();
    }

    public ConfigBase(Map<String, Object> configs) {
        this.props =
                ConfigSource.newSource(getEnvKeyPrefixes()).loadFromMap(configs).toProperties();
        initialize();
    }

    public ConfigBase() {
        this.props = ConfigSource.newSource(getEnvKeyPrefixes())
                .loadFromEnvVariables()
                .toProperties();
        initialize();
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

    /**
     * Override this to do setup in the constructor.
     */
    protected void doSetup() {}

    /**
     * Override this to allow warning for deprecated or removed configs.
     */
    protected List<String> deprecatedConfigs() {
        return List.of();
    }

    private void initialize() {
        doSetup();
        for (String config : deprecatedConfigs()) {
            if (props.containsKey(config)) {
                log.warn("Detected deprecated or removed config {}.", config);
            }
        }
    }
}
