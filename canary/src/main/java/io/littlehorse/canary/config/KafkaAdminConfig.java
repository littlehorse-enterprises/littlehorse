package io.littlehorse.canary.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;
import java.util.stream.Collectors;

public class KafkaAdminConfig implements Config {
    public static final ConfigDef KAFKA_ADMIN_CONFIGS = AdminClientConfig.configDef();
    private final Map<String, Object> configs;

    public KafkaAdminConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("kafka."))
                .map(entry -> Map.entry(entry.getKey().substring(6), entry.getValue()))
                .filter(entry -> KAFKA_ADMIN_CONFIGS.names().contains(entry.getKey()))
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
