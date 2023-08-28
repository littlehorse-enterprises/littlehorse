package io.littlehorse.sdk.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class LHClientConfigTest {

    Faker faker = new Faker();

    @Test
    void getRandomIdIfItIsUnset() {
        LHConfig config = new LHConfig();

        String result = config.getClientId();

        assertThat(result).matches("client-[a-z0-9]{32}");
    }

    @Test
    void skipSettingRandomIdIfItIsProvided() {
        String expectedClientId = faker.app().name();

        Properties properties = new Properties();
        properties.put(LHConfig.CLIENT_ID_KEY, expectedClientId);

        LHConfig config = new LHConfig(properties);

        String result = config.getClientId();

        assertThat(result).isEqualTo(expectedClientId);
    }

    @Test
    void getOnlyOneRandomId() {
        LHConfig config = new LHConfig();

        String result1 = config.getClientId();
        String result2 = config.getClientId();

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void haveAllConfigs() {
        assertThat(LHConfig.configNames().size()).isEqualTo(9);
    }

    @Test
    void shouldThrowAnExceptionIfTryToModifyConfigNames() {
        assertThrows(UnsupportedOperationException.class, () -> LHConfig.configNames()
                .add(faker.regexify("[a-z]{10}")));
    }
}
