package io.littlehorse.canary.config;

import java.util.Map;
import java.util.stream.Collectors;

public class KafkaConfig implements Config {
    private final Map<String, Object> configs;

    public KafkaConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("kafka."))
                .map(entry -> Map.entry(entry.getKey().substring(6), entry.getValue()))
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
