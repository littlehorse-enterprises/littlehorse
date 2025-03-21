package io.littlehorse.canary.config;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.streams.StreamsConfig;

public class KafkaStreamsConfig implements Config {
    public static final ConfigDef KAFKA_STREAMS_CONFIGS = StreamsConfig.configDef();
    private final Map<String, Object> configs;

    public KafkaStreamsConfig(final Map<String, Object> configs) {
        this.configs = configs.entrySet().stream()
                // is enough only filter this?
                .filter(entry -> entry.getKey().startsWith("kafka."))
                .map(entry -> Map.entry(entry.getKey().substring(6), entry.getValue()))
                // filter or not filter?
                .filter(entry -> KAFKA_STREAMS_CONFIGS.names().contains(entry.getKey()))
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

    public static void main(final String[] args) {
        KAFKA_STREAMS_CONFIGS.withClientSaslSupport().withClientSslSupport();
        System.out.println(KAFKA_STREAMS_CONFIGS.names());
    }
}
