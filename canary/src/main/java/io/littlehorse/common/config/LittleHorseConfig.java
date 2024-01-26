package io.littlehorse.common.config;

import static io.littlehorse.common.config.CanaryConfig.LH_CANARY_PREFIX;
import static java.util.Map.entry;

import io.littlehorse.sdk.common.config.LHConfig;
import java.util.Map;
import java.util.stream.Collectors;

public class LittleHorseConfig implements Config {
    private final Map<String, Object> configs;

    public LittleHorseConfig(Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_PREFIX))
                .map(entry -> {
                    String formattedKey = entry.getKey()
                            .substring(LH_CANARY_PREFIX.length())
                            .toUpperCase()
                            .replace(".", "_")
                            .replace("-", "_");

                    return entry(formattedKey, entry.getValue());
                })
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
