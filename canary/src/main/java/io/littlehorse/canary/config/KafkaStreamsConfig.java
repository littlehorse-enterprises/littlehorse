package io.littlehorse.canary.config;

import static java.util.Map.entry;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.streams.StreamsConfig;

public class KafkaStreamsConfig implements Config {
    private final Map<String, Object> configs;

    public KafkaStreamsConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> StreamsConfig.configDef().names().contains(entry.getKey()))
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
