package io.littlehorse.canary.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LittleHorseConfigTest {

    @Nested
    class WorkerConfigs {
        public static final String INPUT_LHW_KEY = "lhw.task-worker.id";
        public static final String EXPECTED_LHW_KEY = "LHW_TASK_WORKER_ID";
        public static final String EXPECTED_LHW_VALUE = "MY-ID-123";

        @Test
        void shouldCreateCopyOfInputMap() {
            Map<String, Object> input = Map.of(INPUT_LHW_KEY, EXPECTED_LHW_VALUE);
            LittleHorseConfig littleHorseConfig = new LittleHorseConfig(input);

            Map<String, Object> output = littleHorseConfig.toMap();

            assertThat(output).isNotSameAs(input);
        }

        @Test
        void shouldFilterInvalidConfigurations() {
            Map<String, Object> input = Map.of(INPUT_LHW_KEY, EXPECTED_LHW_VALUE, "not.a.valid.key", "To be filtered");
            LittleHorseConfig littleHorseConfig = new LittleHorseConfig(input);

            Map<String, Object> output = littleHorseConfig.toMap();

            assertThat(output).containsExactly(entry(EXPECTED_LHW_KEY, EXPECTED_LHW_VALUE));
        }
    }

    @Nested
    class ClientConfigs {
        public static final String INPUT_LHC_KEY = "lhc.api.host";
        public static final String EXPECTED_LHC_KEY = "LHC_API_HOST";
        public static final String EXPECTED_LHC_VALUE = "localhost";

        @Test
        void shouldCreateCopyOfInputMap() {
            Map<String, Object> input = Map.of(INPUT_LHC_KEY, EXPECTED_LHC_VALUE);
            LittleHorseConfig littleHorseConfig = new LittleHorseConfig(input);

            Map<String, Object> output = littleHorseConfig.toMap();

            assertThat(output).isNotSameAs(input);
        }

        @Test
        void shouldFilterInvalidConfigurations() {
            Map<String, Object> input = Map.of(INPUT_LHC_KEY, EXPECTED_LHC_VALUE, "not.a.valid.key", "To be filtered");
            LittleHorseConfig littleHorseConfig = new LittleHorseConfig(input);

            Map<String, Object> output = littleHorseConfig.toMap();

            assertThat(output).containsExactly(entry(EXPECTED_LHC_KEY, EXPECTED_LHC_VALUE));
        }
    }
}
