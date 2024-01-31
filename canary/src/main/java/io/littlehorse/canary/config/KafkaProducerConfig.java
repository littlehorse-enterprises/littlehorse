package io.littlehorse.canary.config;

import static java.util.Map.entry;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.ProducerConfig;

public class KafkaProducerConfig implements Config {
    private final Map<String, Object> configs;

    public KafkaProducerConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LH_CANARY_KAFKA_PREFIX))
                .map(entry -> entry(entry.getKey().substring(LH_CANARY_KAFKA_PREFIX.length()), entry.getValue()))
                .filter(entry -> ProducerConfig.configNames().contains(entry.getKey()))
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
