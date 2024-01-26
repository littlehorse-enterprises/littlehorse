package io.littlehorse.common.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CanaryConfig implements Config {

    public static final String LH_CANARY_PREFIX = "lh.canary.";
    private final Map<String, Object> configs;

    public CanaryConfig(Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_PREFIX))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public Map<String, Object> toMap() {
        return configs;
    }

    public KafkaAdminConfig toKafkaAdminConfig() {
        return new KafkaAdminConfig(configs);
    }

    public LittleHorseConfig toLittleHorseConfig() {
        return new LittleHorseConfig(configs);
    }

    @Override
    public String toString() {
        return configs.toString();
    }
}
