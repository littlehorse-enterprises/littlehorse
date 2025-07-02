package io.littlehorse.canary.config;

import static java.util.Map.entry;

import io.littlehorse.sdk.common.config.LHConfig;
import java.util.Map;
import java.util.stream.Collectors;

public class LittleHorseConfig implements Config {
    private final Map<String, Object> configs;

    public LittleHorseConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .map(entry ->
                        entry(entry.getKey().toUpperCase().replace(".", "_").replace("-", "_"), entry.getValue()))
                .filter(entry -> LHConfig.configNames().contains(entry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> toMap() {
        return configs;
    }

    @Override
    public String toString() {
        return configs.toString();
    }
}
