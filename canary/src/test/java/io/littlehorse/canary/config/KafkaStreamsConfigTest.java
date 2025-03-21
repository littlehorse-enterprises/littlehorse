package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KafkaStreamsConfigTest {

    @Test
    void shouldKeepKafkaSecurityConfigs() {
        Map<String, Object> input = new HashMap<>();
        input.put("kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        input.put("kafka.value.serializer", "org.apache.kafka.common.serialization.BytesSerializer");
        input.put("kafka.acks", "all");
        input.put("kafka.client.id", "id");
        input.put("kafka.enable.idempotence", "true");
        input.put("kafka.application.id", "my-app");
        input.put("kafka.state.dir", "/my-store");
        input.put(
                "kafka.default.deserialization.exception.handler",
                "io.littlehorse.canary.aggregator.internal.ProtobufDeserializationExceptionHandler");
        input.put("kafka.bootstrap.servers", "localhost");
        input.put("kafka.security.protocol", "SSL");
        input.put("kafka.ssl.keystore.password", "pay-password");
        input.put("kafka.sasl.kerberos.service.name", "service");
        input.put("kafka.min.insync.replicas", "2");
        input.put("kafka.num.standby.replicas", "2");
        input.put("not.a.key", "not.a.value");
        input.put("kafka.replication.factor", "1");
        input.put("kafka.enable.auto.commit", "true");

        Map<String, Object> expected = new HashMap<>();
        expected.put("client.id", "id");
        expected.put("application.id", "my-app");
        expected.put("state.dir", "/my-store");
        expected.put(
                "default.deserialization.exception.handler",
                "io.littlehorse.canary.aggregator.internal.ProtobufDeserializationExceptionHandler");
        expected.put("sasl.kerberos.service.name", "service");
        expected.put("ssl.keystore.password", "pay-password");
        expected.put("bootstrap.servers", "localhost");
        expected.put("security.protocol", "SSL");
        expected.put("num.standby.replicas", "2");
        expected.put("replication.factor", "1");

        KafkaStreamsConfig kafkaStreamsConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaStreamsConfig.toMap();

        assertThat(output).isEqualTo(expected);
    }

    @Test
    void shouldKeepKafkaProducer() {
        Map<String, Object> input = new HashMap<>();
        input.put("kafka.producer.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        input.put("kafka.producer.value.serializer", "org.apache.kafka.common.serialization.BytesSerializer");
        input.put("kafka.producer.acks", "all");
        input.put("kafka.producer.enable.idempotence", "true");
        input.put("not.a.key", "not.a.value");

        Map<String, Object> expected = new HashMap<>();
        expected.put("producer.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        expected.put("producer.value.serializer", "org.apache.kafka.common.serialization.BytesSerializer");
        expected.put("producer.acks", "all");
        expected.put("producer.enable.idempotence", "true");
        KafkaStreamsConfig kafkaStreamsConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaStreamsConfig.toMap();

        assertThat(output).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"consumer", "restore.consumer", "main.consumer", "global.consumer"})
    void shouldKeepKafkaConsumer(String consumerPrefix) {
        Map<String, Object> input = new HashMap<>();
        input.put(
                "kafka.%s.key.deserializer".formatted(consumerPrefix),
                "org.apache.kafka.common.serialization.StringDeserializer");
        input.put(
                "kafka.%s.value.deserializer".formatted(consumerPrefix),
                "org.apache.kafka.common.serialization.BytesDeserializer");
        input.put("kafka.%s.max.poll.records".formatted(consumerPrefix), "2");
        input.put("kafka.%s.enable.auto.commit".formatted(consumerPrefix), "true");
        input.put("not.a.key", "not.a.value");

        Map<String, Object> expected = new HashMap<>();
        expected.put(
                "%s.key.deserializer".formatted(consumerPrefix),
                "org.apache.kafka.common.serialization.StringDeserializer");
        expected.put(
                "%s.value.deserializer".formatted(consumerPrefix),
                "org.apache.kafka.common.serialization.BytesDeserializer");
        expected.put("%s.max.poll.records".formatted(consumerPrefix), "2");
        expected.put("%s.enable.auto.commit".formatted(consumerPrefix), "true");
        KafkaStreamsConfig kafkaStreamsConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaStreamsConfig.toMap();

        assertThat(output).isEqualTo(expected);
    }

    @Test
    void shouldKeepKafkaAdmin() {
        Map<String, Object> input = new HashMap<>();
        input.put("kafka.admin.security.providers", "a-provider");
        input.put("kafka.admin.metrics.recording.level", "a-level");
        input.put("not.a.key", "not.a.value");

        Map<String, Object> expected = new HashMap<>();
        expected.put("admin.security.providers", "a-provider");
        expected.put("admin.metrics.recording.level", "a-level");
        KafkaStreamsConfig kafkaStreamsConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaStreamsConfig.toMap();

        assertThat(output).isEqualTo(expected);
    }
}
