package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class KafkaStreamsConfigTest {

    public static final String EXPECTED_KEY = "bootstrap.servers";
    public static final String EXPECTED_VALUE = "localhost:9092";

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of(EXPECTED_KEY, EXPECTED_VALUE);
        KafkaStreamsConfig kafkaAdminConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();
        log.info("Configs: {}", output);

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void filterMap() {
        Map<String, Object> input = Map.of(EXPECTED_KEY, EXPECTED_VALUE, "not.a.valid.key", "To be filtered");
        KafkaStreamsConfig kafkaAdminConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }

    @Test
    void mustKeepProducerConfigs() {
        Map<String, Object> input = Map.of(
                "application.id", "id",
                "state.dir", "/tmp/canaryState",
                "not.a.key", "not.a.value");
        Map<String, Object> expected = Map.of(
                "application.id", "id",
                "state.dir", "/tmp/canaryState");
        KafkaStreamsConfig kafkaAdminConfig = new KafkaStreamsConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();
        log.info("Configs: {}", output);

        assertThat(output).isEqualTo(expected);
    }
}
