package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class KafkaAdminConfigTest {

    @Test
    void shouldKeepKafkaSecurityConfigs() {
        Map<String, Object> input = Map.of(
                "kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "kafka.value.serializer", "org.apache.kafka.common.serialization.BytesSerializer",
                "kafka.acks", "all",
                "kafka.client.id", "id",
                "kafka.enable.idempotence", "true",
                "kafka.bootstrap.servers", "localhost",
                "kafka.security.protocol", "SSL",
                "kafka.ssl.keystore.password", "pay-password",
                "kafka.sasl.kerberos.service.name", "service",
                "not.a.key", "not.a.value");
        Map<String, Object> expected = Map.of(
                "client.id", "id",
                "sasl.kerberos.service.name", "service",
                "ssl.keystore.password", "pay-password",
                "bootstrap.servers", "localhost",
                "security.protocol", "SSL");
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).isEqualTo(expected);
    }
}
