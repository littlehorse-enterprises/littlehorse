package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CanaryConfigTest {

    public static final String KEY = "lh.canary.test";
    public static final String EXPECTED_KEY = "test";
    public static final String EXPECTED_VALUE = "test";

    @Test
    void shouldCreateCopyOfInputMap() {
        Map<String, Object> input = Map.of(KEY, EXPECTED_VALUE);
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).isNotSameAs(input);
    }

    @Test
    void shouldFilterInvalidConfigurations() {
        Map<String, Object> input = Map.of(KEY, EXPECTED_VALUE, "not.a.valid.key", "to be filtered");
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).containsExactly(entry(EXPECTED_KEY, EXPECTED_VALUE));
    }

    @Test
    void shouldParseCommonTags() {
        Map<String, Object> input = Map.of(
                "lh.canary.metrics.common.tags.application_id", "my_id",
                "lh.canary.metrics.common.tags.extra", "extra_tag");

        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, String> output = canaryConfig.getCommonTags();

        assertThat(output).contains(entry("application_id", "my_id"), entry("extra", "extra_tag"));
    }

    @Test
    void shouldParseMetronomeExtraTags() {
        Map<String, Object> input = Map.of("lh.canary.metronome.beat.extra.tags.my_tag", "extra_tag");

        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, String> output = canaryConfig.getMetronomeBeatExtraTags();

        assertThat(output).contains(entry("my_tag", "extra_tag"));
    }

    @Test
    void shouldInitializeMetronomeExtraTags() {
        CanaryConfig canaryConfig = new CanaryConfig(Map.of());

        Map<String, String> output = canaryConfig.getMetronomeBeatExtraTags();

        assertThat(output).isEmpty();
        assertThat(output).isNotNull();
    }

    @Test
    void shouldThrowExceptionIfConfigurationIsNotFound() {
        CanaryConfig canaryConfig = new CanaryConfig(Map.of());

        IllegalArgumentException result =
                assertThrows(IllegalArgumentException.class, () -> canaryConfig.getConfig("my.config"));

        assertThat(result.getMessage()).isEqualTo("Configuration 'lh.canary.my.config' not found");
    }
}
