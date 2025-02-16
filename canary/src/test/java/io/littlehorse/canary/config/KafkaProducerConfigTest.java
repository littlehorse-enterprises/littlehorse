package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class KafkaProducerConfigTest {

    public static final String EXPECTED_KEY = "bootstrap.servers";
    public static final String EXPECTED_VALUE = "localhost:9092";

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of("kafka." + EXPECTED_KEY, EXPECTED_VALUE);
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();
        log.info("Configs: {}", output);

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void filterMap() {
        Map<String, Object> input =
                Map.of("kafka." + EXPECTED_KEY, EXPECTED_VALUE, "not.a.valid.key", "To be filtered");
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }

    @Test
    void mustKeepKafkaConfigs() {
        Map<String, Object> input = Map.of(
                "kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "kafka.value.serializer", "org.apache.kafka.common.serialization.BytesSerializer",
                "kafka.acks", "all",
                "kafka.client.id", "id",
                "kafka.enable.idempotence", "true",
                "not.a.key", "not.a.value");
        Map<String, Object> expected = Map.of(
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.BytesSerializer",
                "acks", "all",
                "client.id", "id",
                "enable.idempotence", "true");
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();
        log.info("Configs: {}", output);

        assertThat(output).isEqualTo(expected);
    }
}
