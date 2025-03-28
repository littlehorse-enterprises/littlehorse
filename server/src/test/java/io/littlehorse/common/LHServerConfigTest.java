package io.littlehorse.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.server.auth.AuthorizationProtocol;
import io.littlehorse.server.auth.OAuthConfig;
import io.littlehorse.server.listener.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import net.datafaker.Faker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LHServerConfigTest {

    private static final String LHS_LISTENERS_AUTHENTICATION_MAP = "LHS_LISTENERS_AUTHENTICATION_MAP";
    private static final String LHS_LISTENERS_PROTOCOL_MAP = "LHS_LISTENERS_PROTOCOL_MAP";
    private static final String LHS_LISTENERS = "LHS_LISTENERS";
    private static final String LHS_ADVERTISED_LISTENERS = "LHS_ADVERTISED_LISTENERS";

    Faker faker = new Faker();

    @Nested
    class GetListeners {

        @Test
        void validateListeners() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "PLAIN:5000");
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN");

            LHServerConfig config = new LHServerConfig(properties);

            List<ServerListenerConfig> result = assertDoesNotThrow(config::getListeners);

            assertThat(result)
                    .containsExactly(ServerListenerConfig.builder()
                            .name("PLAIN")
                            .port(5000)
                            .protocol(ListenerProtocol.PLAIN)
                            .config(config)
                            .authorizationProtocol(AuthorizationProtocol.NONE)
                            .build());
        }

        @Test
        void loadProtocolsForListeners() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "PLAIN_1:5000");
            properties.put(LHS_LISTENERS_AUTHENTICATION_MAP, "PLAIN_1:OAUTH");
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN_1:PLAIN");

            LHServerConfig config = new LHServerConfig(properties);

            List<ServerListenerConfig> result = assertDoesNotThrow(config::getListeners);

            assertThat(result.get(0).getProtocol()).isEqualTo(ListenerProtocol.PLAIN);
            assertThat(result.get(0).getAuthorizationProtocol()).isEqualTo(AuthorizationProtocol.OAUTH);
        }

        @Test
        void throwsExceptionIfAuthenticationProtocolIsMTLSButListenerProtocolIsNot() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "LISTENER_1:5000");
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "LISTENER_1:PLAIN");
            properties.put(LHS_LISTENERS_AUTHENTICATION_MAP, "LISTENER_1:MTLS");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListeners);
        }

        @Test
        void shouldAllowMTLSAuthenticationProtocolWhenListenerProtocolIsAlsoMTLS() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "LISTENER_1:5000");
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "LISTENER_1:MTLS");
            properties.put(LHS_LISTENERS_AUTHENTICATION_MAP, "LISTENER_1:MTLS");

            LHServerConfig config = new LHServerConfig(properties);

            List<ServerListenerConfig> result = assertDoesNotThrow(config::getListeners);

            assertThat(result.get(0))
                    .usingRecursiveComparison()
                    .isEqualTo(ServerListenerConfig.builder()
                            .name("LISTENER_1")
                            .port(5000)
                            .protocol(ListenerProtocol.MTLS)
                            .config(config)
                            .authorizationProtocol(AuthorizationProtocol.MTLS)
                            .build());
        }

        @Test
        void validateSeveralListeners() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "PLAIN:5000,MTLS:6000,OAUTH:7000");
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN,MTLS:MTLS,OAUTH:TLS");

            LHServerConfig config = new LHServerConfig(properties);

            List<ServerListenerConfig> result = assertDoesNotThrow(config::getListeners);

            assertThat(result)
                    .containsAll(List.of(
                            ServerListenerConfig.builder()
                                    .name("PLAIN")
                                    .port(5000)
                                    .protocol(ListenerProtocol.PLAIN)
                                    .config(config)
                                    .authorizationProtocol(AuthorizationProtocol.NONE)
                                    .build(),
                            ServerListenerConfig.builder()
                                    .name("MTLS")
                                    .port(6000)
                                    .protocol(ListenerProtocol.MTLS)
                                    .config(config)
                                    .authorizationProtocol(AuthorizationProtocol.NONE)
                                    .build(),
                            ServerListenerConfig.builder()
                                    .name("OAUTH")
                                    .port(7000)
                                    .protocol(ListenerProtocol.TLS)
                                    .config(config)
                                    .authorizationProtocol(AuthorizationProtocol.NONE)
                                    .build()));
        }

        @Test
        void throwsAnExceptionIfTwoDifferentListenersHaveTheSamePort() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "PLAIN:5000,MTLS:5000");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListeners);
        }

        @Test
        void throwsAnExceptionIfListenersIsNotValid() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "localhost5000");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListeners);
        }

        @Test
        void throwsAnExceptionIfOneListenerInNotValid() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "localhost:5000,localhost6000");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListeners);
        }

        @Test
        void throwsAnExceptionIfTheProtocolIsMissing() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS, "PLAIN_1:5000,PLAIN_2:6000");
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN_1:PLAIN");

            LHServerConfig config = new LHServerConfig(properties);

            List<ServerListenerConfig> result = assertDoesNotThrow(config::getListeners);

            assertThat(result)
                    .containsAll(List.of(
                            ServerListenerConfig.builder()
                                    .name("PLAIN_1")
                                    .port(5000)
                                    .protocol(ListenerProtocol.PLAIN)
                                    .config(config)
                                    .authorizationProtocol(AuthorizationProtocol.NONE)
                                    .build(),
                            ServerListenerConfig.builder()
                                    .name("PLAIN_2")
                                    .port(6000)
                                    .protocol(ListenerProtocol.PLAIN)
                                    .config(config)
                                    .authorizationProtocol(AuthorizationProtocol.NONE)
                                    .build()));
        }
    }

    @Nested
    class GetAdvertisedListener {

        @Test
        void validateAdvertisedListeners() {
            Properties properties = new Properties();
            properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN://localhost:5000");

            LHServerConfig config = new LHServerConfig(properties);

            List<AdvertisedListenerConfig> result = assertDoesNotThrow(config::getAdvertisedListeners);

            assertThat(result)
                    .containsExactly(AdvertisedListenerConfig.builder()
                            .name("PLAIN")
                            .host("localhost")
                            .port(5000)
                            .build());
        }

        @Test
        void itAllowsAdvertisedListenersWithTheSamePort() {
            Properties properties = new Properties();
            properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN://localhost:5000,EXTERNAL://insecure.external.com:5000");

            LHServerConfig config = new LHServerConfig(properties);

            List<AdvertisedListenerConfig> result = assertDoesNotThrow(config::getAdvertisedListeners);

            assertThat(result)
                    .containsExactly(
                            AdvertisedListenerConfig.builder()
                                    .name("PLAIN")
                                    .host("localhost")
                                    .port(5000)
                                    .build(),
                            AdvertisedListenerConfig.builder()
                                    .name("EXTERNAL")
                                    .host("insecure.external.com")
                                    .port(5000)
                                    .build());
        }

        @Test
        void validateSeveralAdvertisedListeners() {
            Properties properties = new Properties();
            properties.put(
                    LHS_ADVERTISED_LISTENERS,
                    "PLAIN://localhost:5000,MTLS://secure.localhost:6000,OAUTH://oauth.localhost:7000");

            LHServerConfig config = new LHServerConfig(properties);

            List<AdvertisedListenerConfig> result = assertDoesNotThrow(config::getAdvertisedListeners);

            assertThat(result)
                    .containsAll(List.of(
                            AdvertisedListenerConfig.builder()
                                    .name("PLAIN")
                                    .host("localhost")
                                    .port(5000)
                                    .build(),
                            AdvertisedListenerConfig.builder()
                                    .name("MTLS")
                                    .host("secure.localhost")
                                    .port(6000)
                                    .build(),
                            AdvertisedListenerConfig.builder()
                                    .name("OAUTH")
                                    .host("oauth.localhost")
                                    .port(7000)
                                    .build()));
        }

        @Test
        void throwsAnExceptionIfAdvertisedListenersIsNotValid() {
            Properties properties = new Properties();
            properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN://localhost5000");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getAdvertisedListeners);
        }

        @Test
        void throwsAnExceptionIfOneAdvertisedListenerInNotValid() {
            Properties properties = new Properties();
            properties.put(LHS_ADVERTISED_LISTENERS, "PLAIN://localhost:5000,MTLS:/localhost:6000");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getAdvertisedListeners);
        }
    }

    @Nested
    class GetListenersProtocolMap {

        @Test
        void validateProtocolListenersIfMissing() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN,MTLS");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListenersProtocolMap);
        }

        @Test
        void validateProtocolListenersIfInvalidChar() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN$MTLS:MTLS");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListenersProtocolMap);
        }

        @Test
        void validateProtocolDoesNotExists() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN,MTLS:MTLS,NOT_EXISTS:NOT_EXISTS");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListenersProtocolMap);
        }

        @Test
        void loadProtocolListeners() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS_PROTOCOL_MAP, "PLAIN:PLAIN");

            LHServerConfig config = new LHServerConfig(properties);

            Map<String, ListenerProtocol> result = assertDoesNotThrow(config::getListenersProtocolMap);

            assertThat(result.get("PLAIN")).isEqualTo(ListenerProtocol.PLAIN);
        }
    }

    @Nested
    class GetListenersAuthorizationMap {

        @Test
        void validateAuthProtocolDoesNotExists() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS_AUTHENTICATION_MAP, "LISTENER_1:OAUTH,LISTENER_2:NOT_EXISTS");

            LHServerConfig config = new LHServerConfig(properties);

            assertThrows(LHMisconfigurationException.class, config::getListenersAuthorizationMap);
        }

        @Test
        void loadAuthProtocolListeners() {
            Properties properties = new Properties();
            properties.put(LHS_LISTENERS_AUTHENTICATION_MAP, "LISTENER_1:OAUTH,LISTENER_2:MTLS,LISTENER_3:NONE");

            LHServerConfig config = new LHServerConfig(properties);

            Map<String, AuthorizationProtocol> result = assertDoesNotThrow(config::getListenersAuthorizationMap);

            assertThat(result.get("LISTENER_1")).isEqualTo(AuthorizationProtocol.OAUTH);
            assertThat(result.get("LISTENER_2")).isEqualTo(AuthorizationProtocol.MTLS);
            assertThat(result.get("LISTENER_3")).isEqualTo(AuthorizationProtocol.NONE);
        }
    }

    @Nested
    class GetOAuthConfigByListener {

        @Test
        void loadOAuthConfigFromProperties() {
            Properties properties = new Properties();
            String clientId = UUID.randomUUID().toString();
            String clientSecret = UUID.randomUUID().toString();
            String server =
                    "https://" + faker.internet().url() + "/" + faker.internet().slug();

            properties.put("LHS_OAUTH_CLIENT_ID", clientId);
            properties.put("LHS_OAUTH_CLIENT_SECRET", clientSecret);
            properties.put("LHS_OAUTH_INTROSPECT_URL", server);

            LHServerConfig config = new LHServerConfig(properties);

            assertThat(config.getOAuthConfig())
                    .isEqualTo(OAuthConfig.builder()
                            .introspectionEndpointURI(URI.create(server))
                            .clientId(clientId)
                            .clientSecret(clientSecret)
                            .build());
        }

        @Test
        void throwsExceptionIfItIsAnInvalidURLWhenLoadingOAuthSettings() {
            String clientId = UUID.randomUUID().toString();
            String clientSecret = UUID.randomUUID().toString();
            String server = "%^";

            Properties properties = new Properties();
            properties.put("LHS_OAUTH_CLIENT_ID", clientId);
            properties.put("LHS_OAUTH_CLIENT_SECRET", clientSecret);
            properties.put("LHS_OAUTH_INTROSPECT_URL", server);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(config::getOAuthConfig)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage("Malformed URL check LHS_OAUTH_INTROSPECT_URL");
        }

        @Test
        void loadOAuthCredentialsFromFile() throws IOException {
            String clientId = UUID.randomUUID().toString();
            String clientSecret = UUID.randomUUID().toString();
            String server =
                    "https://" + faker.internet().url() + "/" + faker.internet().slug();
            String fileClientId = "/tmp/test-" + UUID.randomUUID() + ".txt";
            String fileClientSecret = "/tmp/test-" + UUID.randomUUID() + ".txt";

            Files.writeString(Path.of(fileClientId), clientId);
            Files.writeString(Path.of(fileClientSecret), clientSecret);

            Properties properties = new Properties();
            properties.put("LHS_OAUTH_CLIENT_ID_FILE", fileClientId);
            properties.put("LHS_OAUTH_CLIENT_SECRET_FILE", fileClientSecret);
            properties.put("LHS_OAUTH_INTROSPECT_URL", server);
            properties.put("LHS_OAUTH_CLIENT_ID", "some-random-id");
            properties.put("LHS_OAUTH_CLIENT_SECRET", "some-random-secret");

            LHServerConfig config = new LHServerConfig(properties);
            assertThat(config.getOAuthConfig())
                    .isEqualTo(OAuthConfig.builder()
                            .clientId(clientId)
                            .clientSecret(clientSecret)
                            .introspectionEndpointURI(URI.create(server))
                            .build());
        }

        @Test
        void throwsExceptionIfOAuthServerUrlIsMissing() {
            Properties properties = new Properties();
            String clientId = UUID.randomUUID().toString();
            String clientSecret = UUID.randomUUID().toString();

            properties.put("LHS_OAUTH_CLIENT_ID", clientId);
            properties.put("LHS_OAUTH_CLIENT_SECRET", clientSecret);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(config::getOAuthConfig)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "OAuth configuration called but not provided. Check missing client id, client secret or introspection endpoint url");
        }

        @Test
        void throwsExceptionIfOAuthClientIdIsMissing() {
            Properties properties = new Properties();
            String clientSecret = UUID.randomUUID().toString();
            String server =
                    "https://" + faker.internet().url() + "/" + faker.internet().slug();

            properties.put("LHS_OAUTH_CLIENT_SECRET", clientSecret);
            properties.put("LHS_OAUTH_INTROSPECT_URL", server);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(config::getOAuthConfig)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "OAuth configuration called but not provided. Check missing client id, client secret or introspection endpoint url");
        }

        @Test
        void throwsExceptionIfOAuthClientSecretIsMissing() {
            Properties properties = new Properties();
            String clientId = UUID.randomUUID().toString();
            String server =
                    "https://" + faker.internet().url() + "/" + faker.internet().slug();

            properties.put("LHS_OAUTH_CLIENT_ID", clientId);
            properties.put("LHS_OAUTH_INTROSPECT_URL", server);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(config::getOAuthConfig)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "OAuth configuration called but not provided. Check missing client id, client secret or introspection endpoint url");
        }
    }

    @Nested
    class GetTLSConfiguration {

        @Test
        void throwsExceptionIfCertificateIsMissing() {
            String mockFilePath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_KEY", mockFilePath);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener TEST was configured to use TLS but LHS_LISTENER_TEST_KEY and/or LHS_LISTENER_TEST_CERT are missing");
        }

        @Test
        void throwsExceptionIfKeyIsMissing() {
            String mockFilePath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_CERT", mockFilePath);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener TEST was configured to use TLS but LHS_LISTENER_TEST_KEY and/or LHS_LISTENER_TEST_CERT are missing");
        }

        @Test
        void throwsExceptionIfFileIsNotValid() {
            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_CERT", "/path-that-doesn't-exist/some-cert.pem");

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage("Invalid configuration: File location specified on LHS_LISTENER_TEST_CERT is invalid");
        }

        @Test
        void shouldLoadTLSConfigByListenerName() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();
            String mockKeyPath =
                    getClass().getClassLoader().getResource("MockCert2.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_CERT", mockCertPath);
            properties.put("LHS_LISTENER_TEST_KEY", mockKeyPath);

            LHServerConfig config = new LHServerConfig(properties);

            TLSConfig tlsConfig = config.getTLSConfiguration("TEST");

            assertThat(tlsConfig).isEqualTo(new TLSConfig(new File(mockCertPath), new File(mockKeyPath)));
        }
    }

    @Nested
    class GetMTLSConfiguration {

        @Test
        void throwsExceptionIfCertificateIsMissing() {
            String mockFilePath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_KEY", mockFilePath);
            properties.put("LHS_CA_CERT", mockFilePath);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getMTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener TEST was configured to use MTLS but LHS_LISTENER_TEST_KEY, LHS_LISTENER_TEST_CERT and/or LHS_CA_CERT are missing");
        }

        @Test
        void throwsExceptionIfKeyIsMissing() {
            String mockFilePath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_CERT", mockFilePath);
            properties.put("LHS_CA_CERT", mockFilePath);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getMTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener TEST was configured to use MTLS but LHS_LISTENER_TEST_KEY, LHS_LISTENER_TEST_CERT and/or LHS_CA_CERT are missing");
        }

        @Test
        void throwsExceptionIfCACertIsMissing() {
            String mockFilePath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_KEY", mockFilePath);
            properties.put("LHS_LISTENER_TEST_CERT", mockFilePath);

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getMTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener TEST was configured to use MTLS but LHS_LISTENER_TEST_KEY, LHS_LISTENER_TEST_CERT and/or LHS_CA_CERT are missing");
        }

        @Test
        void shouldLoadMTLSConfigByListenerName() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();
            String mockKeyPath =
                    getClass().getClassLoader().getResource("MockCert2.pem").getPath();
            String mockCACertPath =
                    getClass().getClassLoader().getResource("MockCert2.pem").getPath();

            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_CERT", mockCertPath);
            properties.put("LHS_LISTENER_TEST_KEY", mockKeyPath);
            properties.put("LHS_CA_CERT", mockCACertPath);

            LHServerConfig config = new LHServerConfig(properties);

            TLSConfig tlsConfig = config.getMTLSConfiguration("TEST");

            assertThat(tlsConfig)
                    .isEqualTo(new MTLSConfig(new File(mockCACertPath), new File(mockCertPath), new File(mockKeyPath)));
        }

        @Test
        void throwsExceptionIfFileIsNotValid() {
            Properties properties = new Properties();

            properties.put("LHS_LISTENER_TEST_CERT", "/path-that-doesn't-exist/some-cert.pem");

            LHServerConfig config = new LHServerConfig(properties);

            assertThatThrownBy(() -> config.getMTLSConfiguration("TEST"))
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage("Invalid configuration: File location specified on LHS_LISTENER_TEST_CERT is invalid");
        }
    }
}
