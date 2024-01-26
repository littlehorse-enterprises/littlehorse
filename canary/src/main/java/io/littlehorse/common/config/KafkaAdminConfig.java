package io.littlehorse.common.config;

import static io.littlehorse.common.config.CanaryConfig.LH_CANARY_PREFIX;
import static java.util.Map.entry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClientConfig;

public class KafkaAdminConfig implements Config {
    public static final String LH_CANARY_KAFKA_PREFIX = LH_CANARY_PREFIX + "kafka.";
    private final Map<String, Object> configs;

    public KafkaAdminConfig(Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_KAFKA_PREFIX))
                .map(entry -> entry(entry.getKey().substring(LH_CANARY_KAFKA_PREFIX.length()), entry.getValue()))
                .filter(entry -> AdminClientConfig.configNames().contains(entry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
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
