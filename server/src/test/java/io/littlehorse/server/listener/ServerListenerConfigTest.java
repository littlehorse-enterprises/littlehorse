package io.littlehorse.server.listener;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ServerListenerConfigTest {

    @Nested
    class GetCredentials {

        @Test
        void shouldThrowExceptionWhenProtocolIsMTLSButNoClientCACertIsProvided() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            ServerListenerConfig listenerConfig = ServerListenerConfig.builder()
                    .name("some-listener")
                    .protocol(ListenerProtocol.MTLS)
                    .clientsCACert(null)
                    .certificateKey(new File(mockCertPath))
                    .certificate(new File(mockCertPath))
                    .config(mock())
                    .build();

            assertThatThrownBy(listenerConfig::getCredentials)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener some-listener was configured to use MTLS but certificate, key and/or client CA cert are missing");
        }

        @Test
        void shouldThrowExceptionWhenProtocolIsMTLSButNoCertificateIsProvided() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            ServerListenerConfig listenerConfig = ServerListenerConfig.builder()
                    .name("some-listener")
                    .protocol(ListenerProtocol.MTLS)
                    .clientsCACert(new File(mockCertPath))
                    .certificateKey(new File(mockCertPath))
                    .certificate(null)
                    .config(mock())
                    .build();

            assertThatThrownBy(listenerConfig::getCredentials)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener some-listener was configured to use MTLS but certificate, key and/or client CA cert are missing");
        }

        @Test
        void shouldThrowExceptionWhenProtocolIsMTLSButNoKeyIsProvided() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            ServerListenerConfig listenerConfig = ServerListenerConfig.builder()
                    .name("some-listener")
                    .protocol(ListenerProtocol.MTLS)
                    .clientsCACert(new File(mockCertPath))
                    .certificateKey(null)
                    .certificate(new File(mockCertPath))
                    .config(mock())
                    .build();

            assertThatThrownBy(listenerConfig::getCredentials)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener some-listener was configured to use MTLS but certificate, key and/or client CA cert are missing");
        }

        @Test
        void shouldThrowExceptionWhenProtocolIsTLSButNoCertificateIsProvided() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            ServerListenerConfig listenerConfig = ServerListenerConfig.builder()
                    .name("some-listener")
                    .protocol(ListenerProtocol.TLS)
                    .certificate(null)
                    .certificateKey(new File(mockCertPath))
                    .config(mock())
                    .build();

            assertThatThrownBy(listenerConfig::getCredentials)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener some-listener was configured to use TLS but certificate and/or key are missing");
        }

        @Test
        void shouldThrowExceptionWhenProtocolIsTLSButNoKeyIsProvided() {
            String mockCertPath =
                    getClass().getClassLoader().getResource("MockCert.pem").getPath();

            ServerListenerConfig listenerConfig = ServerListenerConfig.builder()
                    .name("some-listener")
                    .protocol(ListenerProtocol.TLS)
                    .certificateKey(null)
                    .certificate(new File(mockCertPath))
                    .config(mock())
                    .build();

            assertThatThrownBy(listenerConfig::getCredentials)
                    .isExactlyInstanceOf(LHMisconfigurationException.class)
                    .hasMessage(
                            "Invalid configuration: Listener some-listener was configured to use TLS but certificate and/or key are missing");
        }
    }
}
