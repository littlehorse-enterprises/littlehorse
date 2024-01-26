package io.littlehorse.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CanaryConfigTest {

    @Test
    void toMapMustCreateCopy() {
        Map<String, Object> input = Map.of();
        CanaryConfig canaryConfig = new CanaryConfig(input);

        Map<String, Object> output = canaryConfig.toMap();

        assertThat(output).isNotSameAs(input);
    }
}
