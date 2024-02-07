package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CanaryConfigTest {

    public static final String KEY = "lh.canary.test";
    public static final String EXPECTED_KEY = "test";
    public static final String EXPECTED_VALUE = "test";

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of(KEY, EXPECTED_VALUE);
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void filterMap() {
        Map<String, Object> input = Map.of(KEY, EXPECTED_VALUE, "not.a.valid.key", "to be filtered");
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }

    @Test
    void getFilterRules() {
        Map<String, Object> input = Map.of(
                "lh.canary.metrics.filter.enable[0]", "test_ms",
                "lh.canary.metrics.filter.enable[1]", "test_ms2");
        CanaryConfig canaryConfig = new CanaryConfig(input);

        List<String> output = canaryConfig.getEnabledMetrics();

        assertThat(output).containsExactlyInAnyOrder("test_ms", "test_ms2");
    }
}
