package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class KafkaProducerConfigTest {

    public static final String EXPECTED_KEY = "bootstrap.servers";
    public static final String EXPECTED_VALUE = "localhost:9092";

    @Test
    void shouldCreateCopyOfInputMap() {
        Map<String, Object> input = Map.of("kafka." + EXPECTED_KEY, EXPECTED_VALUE);
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void shouldFilterInvalidConfigurations() {
        Map<String, Object> input =
                Map.of("kafka." + EXPECTED_KEY, EXPECTED_VALUE, "not.a.valid.key", "To be filtered");
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }

    @Test
    void shouldKeepKafkaConfigs() {
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

        assertThat(output).isEqualTo(expected);
    }
}
