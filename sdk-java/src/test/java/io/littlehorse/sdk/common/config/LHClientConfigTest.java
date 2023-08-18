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
        LHClientConfig config = new LHClientConfig();

        String result = config.getClientId();

        assertThat(result).matches("client-[a-z0-9]{32}");
    }

    @Test
    void skipSettingRandomIdIfItIsProvided() {
        String expectedClientId = faker.app().name();

        Properties properties = new Properties();
        properties.put(LHClientConfig.CLIENT_ID_KEY, expectedClientId);

        LHClientConfig config = new LHClientConfig(properties);

        String result = config.getClientId();

        assertThat(result).isEqualTo(expectedClientId);
    }

    @Test
    void getOnlyOneRandomId() {
        LHClientConfig config = new LHClientConfig();

        String result1 = config.getClientId();
        String result2 = config.getClientId();

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void haveAllConfigs() {
        assertThat(LHClientConfig.configNames().size()).isEqualTo(9);
    }

    @Test
    void shouldThrowAnExceptionIfTryToModifyConfigNames() {
        assertThrows(UnsupportedOperationException.class, () -> LHClientConfig.configNames()
                .add(faker.regexify("[a-z]{10}")));
    }
}
