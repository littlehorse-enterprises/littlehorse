package io.littlehorse.canary.config;

import static org.apache.kafka.streams.StreamsConfig.ADMIN_CLIENT_PREFIX;
import static org.apache.kafka.streams.StreamsConfig.CONSUMER_PREFIX;
import static org.apache.kafka.streams.StreamsConfig.GLOBAL_CONSUMER_PREFIX;
import static org.apache.kafka.streams.StreamsConfig.MAIN_CONSUMER_PREFIX;
import static org.apache.kafka.streams.StreamsConfig.PRODUCER_PREFIX;
import static org.apache.kafka.streams.StreamsConfig.RESTORE_CONSUMER_PREFIX;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.streams.StreamsConfig;

public class KafkaStreamsConfig implements Config {
    private static final ConfigDef KAFKA_STREAMS_CONFIGS =
            StreamsConfig.configDef().withClientSslSupport().withClientSaslSupport();
    private static final ConfigDef KAFKA_PRODUCER_CONFIGS = ProducerConfig.configDef();
    private static final ConfigDef KAFKA_CONSUME_CONFIGS = ConsumerConfig.configDef();
    private static final ConfigDef KAFKA_ADMIN_CONFIGS = AdminClientConfig.configDef();
    private static final String KAFKA_PREFIX = "kafka.";
    private final Map<String, Object> configs;

    public KafkaStreamsConfig(final Map<String, Object> configs) {

        //        StreamsConfig.clientTagPrefix()
        //        StreamsConfig.topicPrefix()

        final List<Predicate<Map.Entry<String, Object>>> filters = List.of(
                entry -> KAFKA_STREAMS_CONFIGS.names().contains(entry.getKey()),
                entry -> entry.getKey().startsWith(PRODUCER_PREFIX)
                        && KAFKA_PRODUCER_CONFIGS
                                .names()
                                .contains(entry.getKey().substring(PRODUCER_PREFIX.length())),
                entry -> entry.getKey().startsWith(CONSUMER_PREFIX)
                        && KAFKA_CONSUME_CONFIGS.names().contains(entry.getKey().substring(CONSUMER_PREFIX.length())),
                entry -> entry.getKey().startsWith(MAIN_CONSUMER_PREFIX)
                        && KAFKA_CONSUME_CONFIGS
                                .names()
                                .contains(entry.getKey().substring(MAIN_CONSUMER_PREFIX.length())),
                entry -> entry.getKey().startsWith(RESTORE_CONSUMER_PREFIX)
                        && KAFKA_CONSUME_CONFIGS
                                .names()
                                .contains(entry.getKey().substring(RESTORE_CONSUMER_PREFIX.length())),
                entry -> entry.getKey().startsWith(GLOBAL_CONSUMER_PREFIX)
                        && KAFKA_CONSUME_CONFIGS
                                .names()
                                .contains(entry.getKey().substring(GLOBAL_CONSUMER_PREFIX.length())),
                entry -> entry.getKey().startsWith(ADMIN_CLIENT_PREFIX)
                        && KAFKA_ADMIN_CONFIGS
                                .names()
                                .contains(entry.getKey().substring(ADMIN_CLIENT_PREFIX.length())));

        this.configs = configs.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(KAFKA_PREFIX))
                .map(entry -> Map.entry(entry.getKey().substring(KAFKA_PREFIX.length()), entry.getValue()))
                .filter(entry -> filters.stream().anyMatch(entryPredicate -> entryPredicate.test(entry)))
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
