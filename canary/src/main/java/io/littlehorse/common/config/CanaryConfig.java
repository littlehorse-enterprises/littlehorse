package io.littlehorse.common.config;

import static java.util.Map.entry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClientConfig;

public class CanaryConfig {

    private static final String LH_CANARY_PREFIX = "lh.canary.";
    private static final String LH_CANARY_KAFKA_PREFIX = LH_CANARY_PREFIX + "kafka.";
    private static final Collector<Entry<String, Object>, ?, Map<String, Object>> MAP_COLLECTOR =
            Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue, (existing, replacement) -> replacement);

    private final Map<String, Object> configs;

    protected CanaryConfig(Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_PREFIX))
                .collect(MAP_COLLECTOR);
    }

    /**
     * Gets configs as a map
     *
     * @return Unmodifiable map
     */
    public Map<String, Object> toMap() {
        return configs;
    }

    /**
     * Return a filtered map for KafkaAdminClient
     *
     * @return Unmodifiable map
     */
    public Map<String, Object> toKafkaAdminConfig() {
        return configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_KAFKA_PREFIX))
                .map(entry -> entry(entry.getKey().substring(LH_CANARY_KAFKA_PREFIX.length()), entry.getValue()))
                .filter(entry -> AdminClientConfig.configNames().contains(entry.getKey()))
                .collect(MAP_COLLECTOR);
    }

    @Override
    public String toString() {
        return configs.toString();
    }
}
