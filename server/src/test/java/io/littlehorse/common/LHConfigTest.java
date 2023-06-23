package io.littlehorse.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.common.exceptions.LHMisconfigurationException;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.auth.OAuthConfig;
import io.littlehorse.server.listener.ListenerProtocol;
import io.littlehorse.server.listener.ServerListenerConfig;
import io.littlehorse.server.listener.TlsConfig;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

public class LHConfigTest {

    private static final String LHS_ADVERTISED_LISTENERS_AUTHORIZATION_MAP =
        "LHS_ADVERTISED_LISTENERS_AUTHORIZATION_MAP";
    private static final String LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP =
        "LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP";
    private static final String LHS_ADVERTISED_LISTENERS = "LHS_ADVERTISED_LISTENERS";

    Faker faker = new Faker();

    @Test
    void validateAdvertisedListeners() {
        Properties properties = new Properties();
        properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN://localhost:5000");
        properties.put(LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN");

        LHConfig config = new LHConfig(properties);

        List<ServerListenerConfig> result = assertDoesNotThrow(
            config::getAdvertisedListeners
        );

        assertThat(result)
            .containsExactly(
                ServerListenerConfig
                    .builder()
                    .name("PLAIN")
                    .host("localhost")
                    .port(5000)
                    .protocol(ListenerProtocol.PLAIN)
                    .config(config)
                    .authorizationProtocol(AuthorizationProtocol.NONE)
                    .build()
            );
    }

    @Test
    void loadProtocolsForAdvertisedListeners() {
        Properties properties = new Properties();
        properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN_1://localhost:5000");
        properties.put(LHS_ADVERTISED_LISTENERS_AUTHORIZATION_MAP, "PLAIN_1:OAUTH");
        properties.put(LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP, "PLAIN_1:PLAIN");

        LHConfig config = new LHConfig(properties);

        List<ServerListenerConfig> result = assertDoesNotThrow(
            config::getAdvertisedListeners
        );

        assertThat(result.get(0).getProtocol()).isEqualTo(ListenerProtocol.PLAIN);
        assertThat(result.get(0).getAuthorizationProtocol())
            .isEqualTo(AuthorizationProtocol.OAUTH);
    }

    @Test
    void validateProtocolAdvertisedListeners() {
        Properties properties = new Properties();
        properties.put(LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN,MTLS");

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListenersProtocolMap
        );
    }

    @Test
    void validateProtocolDoesNotExists() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP,
            "PLAIN:PLAIN,MTLS:MTLS,NOT_EXISTS:NOT_EXISTS"
        );

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListenersProtocolMap
        );
    }

    @Test
    void loadProtocolAdvertisedListeners() {
        Properties properties = new Properties();
        properties.put(LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN");

        LHConfig config = new LHConfig(properties);

        Map<String, ListenerProtocol> result = assertDoesNotThrow(
            config::getAdvertisedListenersProtocolMap
        );

        assertThat(result.get("PLAIN")).isEqualTo(ListenerProtocol.PLAIN);
    }

    @Test
    void validateAuthProtocolDoesNotExists() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS_AUTHORIZATION_MAP,
            "OAUTH:OAUTH,NOT_EXISTS:NOT_EXISTS"
        );

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListenersAuthorizationMap
        );
    }

    @Test
    void loadAuthProtocolAdvertisedListeners() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS_AUTHORIZATION_MAP,
            "OAUTH:OAUTH,BASIC:BASIC"
        );

        LHConfig config = new LHConfig(properties);

        Map<String, AuthorizationProtocol> result = assertDoesNotThrow(
            config::getAdvertisedListenersAuthorizationMap
        );

        assertThat(result.get("BASIC")).isEqualTo(AuthorizationProtocol.BASIC);
    }

    @Test
    void validateSeveralAdvertisedListeners() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS,
            "PLAIN://localhost:5000,MTLS://secure.localhost:6000,OAUTH://oauth.localhost:7000"
        );
        properties.put(
            LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP,
            "PLAIN:PLAIN,MTLS:MTLS,OAUTH:TLS"
        );

        LHConfig config = new LHConfig(properties);

        List<ServerListenerConfig> result = assertDoesNotThrow(
            config::getAdvertisedListeners
        );

        assertThat(result)
            .containsAll(
                List.of(
                    ServerListenerConfig
                        .builder()
                        .name("PLAIN")
                        .host("localhost")
                        .port(5000)
                        .protocol(ListenerProtocol.PLAIN)
                        .config(config)
                        .authorizationProtocol(AuthorizationProtocol.NONE)
                        .build(),
                    ServerListenerConfig
                        .builder()
                        .name("MTLS")
                        .host("secure.localhost")
                        .port(6000)
                        .protocol(ListenerProtocol.MTLS)
                        .config(config)
                        .authorizationProtocol(AuthorizationProtocol.NONE)
                        .build(),
                    ServerListenerConfig
                        .builder()
                        .name("OAUTH")
                        .host("oauth.localhost")
                        .port(7000)
                        .protocol(ListenerProtocol.TLS)
                        .config(config)
                        .authorizationProtocol(AuthorizationProtocol.NONE)
                        .build()
                )
            );
    }

    @Test
    void throwsAnExceptionIfAdvertisedListenersIsNotValid() {
        Properties properties = new Properties();
        properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN://localhost5000");

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListeners
        );
    }

    @Test
    void throwsAnExceptionIfOneAdvertisedListenerInNotValid() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS,
            "PLAIN://localhost:5000,MTLS:/localhost:6000"
        );

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListeners
        );
    }

    @Test
    void throwsAnExceptionIfTwoDifferentListenersHaveTheSamePort() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS,
            "PLAIN://localhost:5000,MTLS://localhost:5000"
        );

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListeners
        );
    }

    @Test
    void throwsAnExceptionIfTheProtocolIsMissing() {
        Properties properties = new Properties();
        properties.put(
            LHS_ADVERTISED_LISTENERS,
            "PLAIN://localhost:5000,MTLS://localhost:6000"
        );
        properties.put(LHS_ADVERTISED_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN");

        LHConfig config = new LHConfig(properties);

        assertThrows(
            LHMisconfigurationException.class,
            config::getAdvertisedListeners
        );
    }

    @Test
    void loadOAuthConfigByListenerName() throws MalformedURLException {
        Properties properties = new Properties();
        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        String server =
            "https://" + faker.internet().url() + "/" + faker.internet().slug();

        properties.put("LHS_ADVERTISED_LISTENER_TEST_CLIENT_ID", clientId);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_CLIENT_SECRET", clientSecret);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_AUTHORIZATION_SERVER", server);

        LHConfig config = new LHConfig(properties);

        assertThat(config.getOAuthConfigByAdvertisedListenerName("TEST"))
            .isEqualTo(
                OAuthConfig
                    .builder()
                    .authorizationServer(URI.create(server))
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build()
            );
    }

    @Test
    void throwsExceptionIfItIsAnInvalidURL() {
        Properties properties = new Properties();
        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        String server = "%^";

        properties.put("LHS_ADVERTISED_LISTENER_TEST_CLIENT_ID", clientId);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_CLIENT_SECRET", clientSecret);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_AUTHORIZATION_SERVER", server);

        LHConfig config = new LHConfig(properties);

        LHMisconfigurationException error = assertThrows(
            LHMisconfigurationException.class,
            () -> config.getOAuthConfigByAdvertisedListenerName("TEST")
        );

        assertThat(error.getMessage()).contains("Malformed URL");
    }

    @Test
    void throwsExceptionIfOAuhConfigIsMissing() {
        Properties properties = new Properties();
        LHConfig config = new LHConfig(properties);

        LHMisconfigurationException error = assertThrows(
            LHMisconfigurationException.class,
            () -> config.getOAuthConfigByAdvertisedListenerName("TEST")
        );

        assertThat(error.getMessage())
            .contains("OAuth configuration called but not provided");
    }

    @Test
    void loadCertConfigByListenerName() {
        Properties properties = new Properties();

        String ca = faker.file().fileName();
        String cert = faker.file().fileName();
        String key = faker.file().fileName();

        properties.put("LHS_ADVERTISED_LISTENER_TEST_CA_CERT", ca);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_CERT", cert);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_KEY", key);

        LHConfig config = new LHConfig(properties);

        assertThat(config.getTlsConfigByAdvertisedListenerName("TEST"))
            .isEqualTo(
                TlsConfig
                    .builder()
                    .caCert(new File(ca))
                    .cert(new File(cert))
                    .key(new File(key))
                    .build()
            );
    }

    @Test
    void loadCertConfigByListenerNameWithoutCA() {
        Properties properties = new Properties();

        String cert = faker.file().fileName();
        String key = faker.file().fileName();

        properties.put("LHS_ADVERTISED_LISTENER_TEST_CERT", cert);
        properties.put("LHS_ADVERTISED_LISTENER_TEST_KEY", key);

        LHConfig config = new LHConfig(properties);

        assertThat(config.getTlsConfigByAdvertisedListenerName("TEST"))
            .isEqualTo(
                TlsConfig.builder().cert(new File(cert)).key(new File(key)).build()
            );
    }

    @Test
    void throwsExceptionIfTlsConfigIsMissing() {
        Properties properties = new Properties();
        LHConfig config = new LHConfig(properties);

        LHMisconfigurationException error = assertThrows(
            LHMisconfigurationException.class,
            () -> config.getTlsConfigByAdvertisedListenerName("TEST")
        );

        assertThat(error.getMessage())
            .contains("TLS configuration called but not provided");
    }
}
