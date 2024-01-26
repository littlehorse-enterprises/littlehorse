package io.littlehorse.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.littlehorse.canary.config.KafkaAdminConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KafkaAdminConfigTest {

    public static final String PREFIX = "lh.canary.kafka.";
    public static final String EXPECTED_KEY = "bootstrap.servers";
    public static final String EXPECTED_VALUE = "localhost:9092";

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of(PREFIX + EXPECTED_KEY, EXPECTED_VALUE);
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void filterMap() {
        Map<String, Object> input = Map.of(PREFIX + EXPECTED_KEY, EXPECTED_VALUE, "not.a.valid.key", "To be filtered");
        KafkaAdminConfig kafkaAdminConfig = new KafkaAdminConfig(input);

        Map<String, Object> output = kafkaAdminConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }
}
