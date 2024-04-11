package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class KafkaConsumerConfigTest {

    public static final String EXPECTED_KEY = "bootstrap.servers";
    public static final String EXPECTED_VALUE = "localhost:9092";

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of(EXPECTED_KEY, EXPECTED_VALUE);
        KafkaConsumerConfig kafkaAdminConfig = new KafkaConsumerConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();
        log.info("Configs: {}", output);

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void filterMap() {
        Map<String, Object> input = Map.of(EXPECTED_KEY, EXPECTED_VALUE, "not.a.valid.key", "To be filtered");
        KafkaConsumerConfig kafkaAdminConfig = new KafkaConsumerConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }

    @Test
    void mustKeepConsumerConfigs() {
        Map<String, Object> input = Map.of(
                "key.deserializer", "org.apache.kafka.common.serialization.BytesDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.BytesDeserializer",
                "auto.offset.reset", "latest",
                "client.id", "id",
                "group.id", "id",
                "enable.auto.commit", "true",
                "not.a.key", "not.a.value");
        Map<String, Object> expected = Map.of(
                "group.id", "id",
                "auto.offset.reset", "latest",
                "enable.auto.commit", "true",
                "key.deserializer", "org.apache.kafka.common.serialization.BytesDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.BytesDeserializer",
                "client.id", "id");
        KafkaConsumerConfig kafkaAdminConfig = new KafkaConsumerConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();
        log.info("Configs: {}", output);

        assertThat(output).isEqualTo(expected);
    }
}
