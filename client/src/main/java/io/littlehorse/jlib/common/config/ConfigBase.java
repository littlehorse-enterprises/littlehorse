package io.littlehorse.jlib.common.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigBase {

    private static final Logger log = LoggerFactory.getLogger(ConfigBase.class);

    protected Properties props;

    public ConfigBase(String propLocation) {
        props = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propLocation);
            props.load(inputStream);
            inputStream.close();
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }

    public ConfigBase(Properties props) {
        this.props = props;
    }

    public ConfigBase(Map<String, Object> configs) {
        props = new Properties();
        props.putAll(configs);
    }

    public ConfigBase() {
        props = new Properties();
        log.info("Loading default config from environment");
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            for (String prefix : getEnvKeyPrefixes()) {
                if (entry.getKey().startsWith(prefix)) {
                    props.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    protected abstract String[] getEnvKeyPrefixes();

    protected String getOrSetDefault(String key, String defaultVal) {
        if (!props.containsKey(key)) {
            log.warn("Config: {} unset, defaulting to: {}", key, defaultVal);
            if (defaultVal != null) props.setProperty(key, defaultVal);
            return defaultVal;
        } else {
            return String.valueOf(props.get(key));
        }
    }
}
