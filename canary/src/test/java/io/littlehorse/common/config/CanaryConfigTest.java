package io.littlehorse.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CanaryConfigTest {

    public static final String EXPECTED_KEY = "lh.canary.test";
    public static final String EXPECTED_VALUE = "test";

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of(EXPECTED_KEY, EXPECTED_VALUE);
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void filterMap() {
        Map<String, Object> input = Map.of(EXPECTED_KEY, EXPECTED_VALUE, "not.a.valid.key", "to be filtered");
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }
}
