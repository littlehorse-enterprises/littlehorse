package io.littlehorse.common.config;

import java.util.Collections;
import java.util.Map;

public class CanaryConfig {

    private final Map<String, Object> configs;

    protected CanaryConfig(Map<String, Object> configs) {
        this.configs = Collections.unmodifiableMap(configs);
    }

    /**
     * Gets configs as a map
     * @return Unmodifiable map
     */
    public Map<String, Object> toMap() {
        return configs;
    }

    @Override
    public String toString() {
        return configs.toString();
    }
}
