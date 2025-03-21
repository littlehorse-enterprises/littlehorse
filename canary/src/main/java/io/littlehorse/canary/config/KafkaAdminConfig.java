package io.littlehorse.canary.config;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.ConfigDef;

public class KafkaAdminConfig implements Config {
    private static final ConfigDef KAFKA_ADMIN_CONFIGS = AdminClientConfig.configDef();
    private static final String KAFKA_PREFIX = "kafka.";
    private final Map<String, Object> configs;

    public KafkaAdminConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(KAFKA_PREFIX))
                .map(entry -> Map.entry(entry.getKey().substring(KAFKA_PREFIX.length()), entry.getValue()))
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
